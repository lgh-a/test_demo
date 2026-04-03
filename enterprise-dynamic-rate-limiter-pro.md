# 企业级动态双策略限流模块（Pro 版）— AI 编辑器上下文规则

## 项目概览

本模块在双策略限流基础上引入**二级重写与降级架构**，支持通过修改 Redis 中的配置实时热更新限流规则，无需重启服务。  
技术栈：**Spring Boot + Spring AOP + Redis + Lua + Jackson**。  
降级策略：Redis 动态规则优先；若 `configKey` 未配置或 Redis 异常，自动降级为注解上的硬编码兜底值。

---

## 目录结构

```

---

## 每日限流规则动态修改后不重置的问题补充

### 问题根因

当前 `FIXED_WINDOW_DAILY` 的实现里：

1. 动态规则存放在 `sys:rate_limit:rules`
2. 当天调用次数存放在单独的 Redis 计数 Key
3. 更新规则时只改了 `capacity`，没有改当天计数 Key

所以当日行为一定是“在旧计数基础上继续累计”，而不是“清零后重新开始”。

例如：

1. 原规则每天 `5` 次
2. 某手机号今天已经用了 `5` 次
3. 管理员把规则改成每天 `10` 次
4. 系统会表现为 `5/10`，而不是重置为 `0/10`

这不是偶发问题，而是当前存储模型的必然结果。

### 不推荐的做法

不要把“更新规则时扫描并删除当天所有计数 Key”作为主方案，原因是：

1. 计数 Key 带有动态维度，例如手机号、用户 ID、IP
2. 管理接口只有 `configKey`，并不知道实际生成了哪些业务计数 Key
3. 依赖 `SCAN` 批量删除在生产环境成本高，而且不够稳定

### 推荐方案：规则版本号切换计数空间

推荐给每日限流规则增加 `version` 概念。

实现思路：

1. `RateLimitRule` 增加 `version`
2. 每次规则修改且需要“立即重置当天额度”时，递增 `version`
3. 每日限流计数 Key 拼接 `date + version`
4. 规则更新后，请求命中新版本 Key，等价于进入新的计数空间
5. 旧版本 Key 不主动删除，等到当天自然过期

推荐 key 形态：

```text
limit:daily:sms:sendSms:13800000000:2026-04-03:v3
```

规则改动后版本变成 `v4`，后续请求就会进入：

```text
limit:daily:sms:sendSms:13800000000:2026-04-03:v4
```

这样就能同时满足：

1. 修改规则后立即生效
2. 需要时可以真正重置当天额度
3. 不需要扫描删除历史 Key

### 具体改造要求

#### 1. 扩展 `RateLimitRule`

建议至少增加：

```java
private Integer version;
```

可选增加：

```java
private Boolean resetOnChange;
```

如果先追求简单，保留 `version` 就够了。

#### 2. 修改 `RateLimitRuleManager.saveRule`

保存规则时不要无脑覆盖，应按下面逻辑处理：

1. 先读取旧规则
2. 判断 `capacity/rate` 是否真的变化
3. 只有在配置变化且需要重置时才递增 `version`
4. 重复提交相同配置时，不要重复递增版本

建议新增接口语义：

```java
saveRule(String configKey, Integer capacity, Integer rate, boolean reset)
```

推荐行为：

1. `reset=true`
   - 递增 `version`
   - 立即切换到新的计数空间
2. `reset=false`
   - 只更新阈值
   - 当天已用次数继续保留

#### 3. 修改 `DynamicRateLimitAspect`

在 `FIXED_WINDOW_DAILY` 分支生成最终 key 时，拼接日期和版本号。

伪代码：

```java
String finalKey = baseKey;
if (StringUtils.hasText(dynamicPart)) {
    finalKey = finalKey + ":" + dynamicPart;
}

if (dynamicRateLimit.limitType() == LimitType.FIXED_WINDOW_DAILY) {
    String date = LocalDate.now().toString();
    int version = dynamicRule != null && dynamicRule.getVersion() != null
            ? dynamicRule.getVersion()
            : 1;
    finalKey = finalKey + ":" + date + ":v" + version;
}
```

这里建议把日期直接拼进 key，而不只依赖 TTL，原因是：

1. Redis 中更容易排查
2. key 语义更清晰
3. 后续做审计或观测更方便

#### 4. 修改管理接口

当前管理接口：

```text
POST /api/demo/admin/updateRule?configKey=...&capacity=...&rate=...
```

建议扩展为：

```text
POST /api/demo/admin/updateRule?configKey=...&capacity=...&rate=...&reset=true
```

明确支持两种业务语义：

1. `reset=false`
   - 修改规则，但不清零当天已用次数
2. `reset=true`
   - 修改规则，并立即重置当天额度

### 为什么推荐这个方案

相对于“直接删 Key”，规则版本方案更合适：

1. 不需要扫描 Redis
2. 不依赖知道所有动态业务 key
3. 多实例下一致性更好
4. 规则修改可以立即生效
5. 旧数据会自然过期，不会长期污染

### 执行结论

如果 AI 编辑器后续继续落地这项改造，必须至少完成：

1. `RateLimitRule` 增加 `version`
2. `RateLimitRuleManager` 支持带 `reset` 的规则保存逻辑
3. `DynamicRateLimitAspect` 在每日窗口 key 中拼接 `date + version`
4. `RateLimitDemoController.updateRule` 增加 `reset` 参数
5. 补充验证用例：
   - 先按 `5` 次打满
   - 更新到 `10` 且 `reset=false`，验证从 `5/10` 继续
   - 更新到 `10` 且 `reset=true`，验证重新从 `0/10` 开始
backend/src/main/
├── resources/
│   └── lua/
│       ├── rate_limit_bucket.lua               # 令牌桶 Lua 脚本
│       └── rate_limit_daily.lua                # 每日固定窗口 Lua 脚本
└── java/com/testdemo/
    ├── annotation/
    │   ├── LimitType.java                      # 策略枚举
    │   └── DynamicRateLimit.java               # 升级版注解（新增 configKey 字段）
    ├── aspect/
    │   └── DynamicRateLimitAspect.java         # AOP 切面（含二级重写逻辑）
    ├── entity/
    │   └── RateLimitRule.java                  # 动态限流规则实体
    ├── config/
    │   └── RateLimitRuleManager.java           # Redis Hash 规则管理器
    ├── common/exception/
    │   └── RateLimitException.java             # 限流业务异常
    └── controller/
        └── RateLimitDemoController.java        # 业务使用示例（含管理员更新接口）
```

---

## 编码规范

- **应该** 在注解中声明 `configKey` 来桥接动态规则，未声明时系统自动使用注解硬编码值。
- **应该** 将动态规则以 JSON 格式存储在 Redis Hash 结构的 `sys:rate_limit:rules` 键下，field 为 `configKey`。
- **应该** 在 `RateLimitRuleManager.getRule()` 捕获所有异常并记录日志，降级返回 `null`，不得向上抛出。
- **应该** 在切面中对 `finalCapacity <= 0` 做防御性放行，避免误配置导致全量封禁。
- **不应该** 在业务代码中直接操作 `sys:rate_limit:rules`，所有读写必须通过 `RateLimitRuleManager`。
- **不应该** 在 `RateLimitRule` 实体中使用基本类型（`int`），必须使用包装类型（`Integer`）以支持 `null` 判断。
- **应该** 在切面构造函数中预加载两个 Lua 脚本，禁止在请求链路中动态加载。

---

## 架构约束

- **二级重写优先级**：Redis 动态规则（`configKey`）> 注解硬编码兜底值（`capacity` / `rate`）。
- **降级条件**：`configKey` 为空、Redis 不可达、JSON 反序列化失败，任一情况均自动降级，不抛出异常。
- **Redis 存储结构**：固定使用 Hash，Key 为 `sys:rate_limit:rules`，field 为业务自定义的 `configKey`，value 为 `RateLimitRule` 的 JSON 序列化字符串。
- **Lua 脚本原子性**：两个脚本的核心操作（HMGET→计算→HMSET→EXPIRE；GET→INCR→EXPIRE）必须在单个脚本内完成。
- **禁止的依赖方向**：`annotation` / `entity` 不得依赖 `aspect` / `config`；`aspect` 可依赖全部其他包。
- **文件覆盖原则**：Pro 版文件直接覆盖同名文件，不得保留旧版切面或注解。

---

## 常用命令

```bash
# 构建
mvn clean package -DskipTests

# 启动
mvn spring-boot:run

# 查看当前所有动态限流规则
redis-cli HGETALL sys:rate_limit:rules

# 手动写入动态规则（为 sms.daily.limit 设置每日上限为 10 次）
redis-cli HSET sys:rate_limit:rules sms.daily.limit '{"capacity":10,"rate":null}'

# 删除某条动态规则（降级回注解默认值）
redis-cli HDEL sys:rate_limit:rules sms.daily.limit

# 调用管理员更新接口（HTTP 示例）
curl -X POST "http://localhost:8080/api/demo/admin/updateRule?configKey=sms.daily.limit&capacity=10"
```

---

## 示例代码片段

### 1. 令牌桶 Lua 脚本

```lua
-- File: backend/src/main/resources/lua/rate_limit_bucket.lua
local key = KEYS[1]
local capacity = tonumber(ARGV[1])
local rate = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local limitInfo = redis.call('HMGET', key, 'tokens', 'timestamp')
local tokens = tonumber(limitInfo[1])
local lastRefreshed = tonumber(limitInfo[2])

if tokens == nil then
    tokens = capacity
    lastRefreshed = now
end

local deltaTokens = math.max(0, (now - lastRefreshed) * rate)
tokens = math.min(capacity, tokens + deltaTokens)

if tokens >= requested then
    tokens = tokens - requested
    redis.call('HMSET', key, 'tokens', tokens, 'timestamp', now)
    local expireTime = math.ceil(capacity / rate) * 2
    redis.call('EXPIRE', key, expireTime)
    return 1 
else
    redis.call('HMSET', key, 'timestamp', now)
    return 0 
end
```

### 2. 每日固定窗口 Lua 脚本

```lua
-- File: backend/src/main/resources/lua/rate_limit_daily.lua
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local expireTime = tonumber(ARGV[2])

local current = redis.call('GET', key)
if current and tonumber(current) >= limit then
    return 0 
end

current = redis.call('INCR', key)
if tonumber(current) == 1 then
    redis.call('EXPIRE', key, expireTime)
end

return 1 
```

### 3. 限流业务异常

```java
// File: backend/src/main/java/com/testdemo/common/exception/RateLimitException.java
package com.testdemo.common.exception;

/**
 * 触发限流时抛出的业务异常
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}
```

### 4. 策略枚举

```java
// File: backend/src/main/java/com/testdemo/annotation/LimitType.java
package com.testdemo.annotation;

/**
 * 限流策略枚举
 */
public enum LimitType {
    /** 令牌桶：适合 QPS 级别的防刷与系统保护 */
    TOKEN_BUCKET,
    /** 固定窗口（按天）：适合业务上的每日配额限制 */
    FIXED_WINDOW_DAILY
}
```

### 5. 动态限流规则实体

```java
// File: backend/src/main/java/com/testdemo/entity/RateLimitRule.java
package com.testdemo.entity;

/**
 * 动态限流规则实体
 */
public class RateLimitRule {
    /** 桶容量 或 每日最大次数 */
    private Integer capacity;
    /** 令牌生成速率 (仅令牌桶策略有效) */
    private Integer rate;

    public RateLimitRule() {}
    public RateLimitRule(Integer capacity, Integer rate) {
        this.capacity = capacity;
        this.rate = rate;
    }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getRate() { return rate; }
    public void setRate(Integer rate) { this.rate = rate; }
}
```

### 6. Redis Hash 规则管理器

```java
// File: backend/src/main/java/com/testdemo/config/RateLimitRuleManager.java
package com.testdemo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testdemo.entity.RateLimitRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 动态规则管理器 (基于 Redis Hash 结构)
 */
@Component
public class RateLimitRuleManager {

    private static final Logger log = LoggerFactory.getLogger(RateLimitRuleManager.class);
    private static final String RULE_HASH_KEY = "sys:rate_limit:rules";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public RateLimitRuleManager(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public RateLimitRule getRule(String configKey) {
        if (!StringUtils.hasText(configKey)) return null;
        try {
            Object ruleJson = stringRedisTemplate.opsForHash().get(RULE_HASH_KEY, configKey);
            if (ruleJson != null) {
                return objectMapper.readValue(ruleJson.toString(), RateLimitRule.class);
            }
        } catch (Exception e) {
            log.error("获取动态限流规则异常, configKey: {}", configKey, e);
        }
        return null;
    }

    public void saveRule(String configKey, Integer capacity, Integer rate) {
        try {
            RateLimitRule rule = new RateLimitRule(capacity, rate);
            String ruleJson = objectMapper.writeValueAsString(rule);
            stringRedisTemplate.opsForHash().put(RULE_HASH_KEY, configKey, ruleJson);
            log.info("动态限流规则已更新 -> configKey: {}, rule: {}", configKey, ruleJson);
        } catch (JsonProcessingException e) {
            log.error("序列化限流规则失败", e);
        }
    }
}
```

### 7. 升级版注解（新增 configKey）

```java
// File: backend/src/main/java/com/testdemo/annotation/DynamicRateLimit.java
package com.testdemo.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicRateLimit {
    
    /** 限流资源的 Key 前缀 */
    String keyPrefix() default "limit:";

    /** 动态标识（支持 Spring EL 表达式），例如 "#userId" */
    String dynamicKey() default "";

    /** * 动态配置的 Key (可选)。
     * 配置此项后，优先从 Redis 获取规则。若未配置或获取失败，则降级使用下方 capacity/rate 默认值。
     */
    String configKey() default "";

    /** 限流策略，默认为令牌桶高并发防刷 */
    LimitType limitType() default LimitType.TOKEN_BUCKET;

    /** 代码硬编码兜底值：桶容量 或 每日最大允许次数 */
    int capacity() default 100;
    
    /** 代码硬编码兜底值：令牌生成速率（个/秒） */
    int rate() default 10;
    
    String message() default "请求过于频繁，请稍后再试";
}
```

### 8. AOP 切面（含二级重写逻辑）

```java
// File: backend/src/main/java/com/testdemo/aspect/DynamicRateLimitAspect.java
package com.testdemo.aspect;

import com.testdemo.annotation.DynamicRateLimit;
import com.testdemo.annotation.LimitType;
import com.testdemo.common.exception.RateLimitException;
import com.testdemo.config.RateLimitRuleManager;
import com.testdemo.entity.RateLimitRule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Aspect
@Component
public class DynamicRateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitRuleManager ruleManager;
    private final DefaultRedisScript<Long> bucketScript;
    private final DefaultRedisScript<Long> dailyScript;
    private final ExpressionParser spelParser = new SpelExpressionParser();

    public DynamicRateLimitAspect(StringRedisTemplate stringRedisTemplate, RateLimitRuleManager ruleManager) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.ruleManager = ruleManager;
        
        this.bucketScript = new DefaultRedisScript<>();
        this.bucketScript.setLocation(new ClassPathResource("lua/rate_limit_bucket.lua"));
        this.bucketScript.setResultType(Long.class);
        
        this.dailyScript = new DefaultRedisScript<>();
        this.dailyScript.setLocation(new ClassPathResource("lua/rate_limit_daily.lua"));
        this.dailyScript.setResultType(Long.class);
    }

    @Around("@annotation(dynamicRateLimit)")
    public Object around(ProceedingJoinPoint point, DynamicRateLimit dynamicRateLimit) throws Throwable {
        String baseKey = dynamicRateLimit.keyPrefix() + point.getSignature().getName();
        String dynamicPart = parseSpel(dynamicRateLimit.dynamicKey(), point);
        String finalKey = StringUtils.hasText(dynamicPart) ? baseKey + ":" + dynamicPart : baseKey;

        // --- 核心：二级重写逻辑，读取动态配置 ---
        int finalCapacity = dynamicRateLimit.capacity();
        int finalRate = dynamicRateLimit.rate();
        
        if (StringUtils.hasText(dynamicRateLimit.configKey())) {
            RateLimitRule dynamicRule = ruleManager.getRule(dynamicRateLimit.configKey());
            if (dynamicRule != null) {
                if (dynamicRule.getCapacity() != null) finalCapacity = dynamicRule.getCapacity();
                if (dynamicRule.getRate() != null) finalRate = dynamicRule.getRate();
            }
        }

        if (finalCapacity <= 0) return point.proceed(); // 防御性放行

        Long result;
        if (dynamicRateLimit.limitType() == LimitType.FIXED_WINDOW_DAILY) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime midnight = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long secondsToMidnight = ChronoUnit.SECONDS.between(now, midnight);
            
            result = stringRedisTemplate.execute(dailyScript, Collections.singletonList(finalKey), 
                    String.valueOf(finalCapacity), String.valueOf(secondsToMidnight));
        } else {
            long nowTime = System.currentTimeMillis() / 1000;
            result = stringRedisTemplate.execute(bucketScript, Collections.singletonList(finalKey), 
                    String.valueOf(finalCapacity), String.valueOf(finalRate), String.valueOf(nowTime), "1");
        }

        if (result == null || result == 0L) {
            throw new RateLimitException(dynamicRateLimit.message());
        }

        return point.proceed();
    }

    private String parseSpel(String spel, ProceedingJoinPoint joinPoint) {
        if (!StringUtils.hasText(spel)) return "";
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }
        try {
            return spelParser.parseExpression(spel).getValue(context, String.class);
        } catch (Exception e) {
            return "unknown"; 
        }
    }
}
```

### 9. 业务使用示例（含管理员更新接口）

```java
// File: backend/src/main/java/com/testdemo/controller/RateLimitDemoController.java
package com.testdemo.controller;

import com.testdemo.annotation.DynamicRateLimit;
import com.testdemo.annotation.LimitType;
import com.testdemo.config.RateLimitRuleManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
public class RateLimitDemoController {

    @Autowired
    private RateLimitRuleManager ruleManager;

    /**
     * 场景：每日业务配额 (固定窗口按天策略)
     * 支持动态修改：只要调用了修改接口，下一次请求瞬间生效！
     */
    @GetMapping("/sendSms")
    @DynamicRateLimit(
        configKey = "sms.daily.limit",  // 桥接动态配置
        keyPrefix = "limit:daily:sms:", 
        dynamicKey = "#phone", 
        limitType = LimitType.FIXED_WINDOW_DAILY,
        capacity = 5,  // 若Redis无配置，则降级为5次
        message = "今日短信发送次数已达上限"
    )
    public String sendSms(@RequestParam String phone) {
        return "短信发送成功";
    }

    /**
     * 仅做演示：模拟后台管理员动态修改限流规则的接口
     */
    @PostMapping("/admin/updateRule")
    public String updateRule(@RequestParam String configKey, @RequestParam Integer capacity, @RequestParam(required = false) Integer rate) {
        ruleManager.saveRule(configKey, capacity, rate);
        return "规则动态更新成功";
    }
}
```
