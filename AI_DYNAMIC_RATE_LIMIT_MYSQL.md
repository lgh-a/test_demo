# 动态限流规则数据库持久化与管理功能开发

## 目标

将当前基于 Redis Hash 存储的动态限流规则迁移到 MySQL 持久化，并新增完整的管理功能（增删改查），实现规则的可视化配置。

本任务只要求修改后端，不要求变更前端业务页面。

---

## 项目上下文

在开始编辑前，AI 编辑器必须基于以下已确认事实执行：

1. 后端项目路径为 `backend`
2. 技术栈为 `Spring Boot 3.2.3 + Java 21 + Spring MVC + MyBatis-Plus + Sa-Token`
3. 当前限流规则存储在 Redis Hash (`sys:rate_limit:rules`) 中
4. 当前限流组件包括：
   - `RateLimitRule` - 规则实体类
   - `RateLimitRuleManager` - Redis 规则管理器
   - `DynamicRateLimitAspect` - 限流切面
   - `DynamicRateLimit` - 限流注解
   - `LimitType` - 限流策略枚举（TOKEN_BUCKET / FIXED_WINDOW_DAILY）
5. 当前已有控制器：
   - `RateLimitDemoController` (`/api/demo/**`) - 限流演示接口
6. 当前 `RateLimitRule` 结构：

```java
public class RateLimitRule {
    private Integer capacity;    // 桶容量/每日最大次数
    private Integer rate;        // 令牌生成速率
    private Integer version;      // 规则版本号
}
```

7. 当前异常统一处理使用 `GlobalExceptionHandler`

---

## 需求分析

### 现有问题

1. 规则存储在 Redis 中，无法持久化，Redis 清除后规则丢失
2. 规则配置需要手动操作 Redis，缺乏可视化界面
3. 规则修改记录无法追溯

### 业务目标

1. 将限流规则持久化到 MySQL
2. 提供规则的增删改查 API
3. 保留 Redis 缓存机制，保证限流性能
4. 支持规则修改后自动刷新缓存

---

## 数据库设计

### 新增表 `sys_rate_limit_rule`

```sql
CREATE TABLE `sys_rate_limit_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '规则配置键，唯一标识',
  `name` VARCHAR(100) DEFAULT NULL COMMENT '规则名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
  `limit_type` VARCHAR(20) NOT NULL DEFAULT 'TOKEN_BUCKET' COMMENT '限流策略：TOKEN_BUCKET-令牌桶，FLEXIBLE-固定窗口每日',
  `capacity` INT NOT NULL DEFAULT 100 COMMENT '登录用户-桶容量/每日最大次数',
  `rate` INT NOT NULL DEFAULT 10 COMMENT '登录用户-令牌生成速率',
  `guest_capacity` INT DEFAULT NULL COMMENT '游客-桶容量/每日最大次数',
  `guest_rate` INT DEFAULT NULL COMMENT '游客-令牌生成速率',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态限流规则表';
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| config_key | VARCHAR(100) | 全局唯一，如 `sms:send`、`game:play` |
| name | VARCHAR(100) | 规则中文名称 |
| description | VARCHAR(500) | 规则用途描述 |
| limit_type | VARCHAR(20) | 限流策略枚举值 |
| capacity | INT | 登录用户 - 桶容量/每日最大次数 |
| rate | INT | 登录用户 - 令牌生成速率(个/秒) |
| guest_capacity | INT | 游客专用容量，null 时降级用 capacity |
| guest_rate | INT | 游客专用速率，null 时降级用 rate |
| status | TINYINT | 1-启用，0-禁用 |
| version | INT | 版本号，用于缓存刷新 |

---

## 新增文件清单

### 1. 实体类

文件：`backend/src/main/java/com/testdemo/entity/RateLimitRule.java`

修改现有实体类，适配数据库字段：

```java
@Schema(description = "动态限流规则")
public class RateLimitRule {
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "规则配置键")
    private String configKey;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "限流策略")
    private String limitType;

    @Schema(description = "登录用户-桶容量/每日最大次数")
    private Integer capacity;

    @Schema(description = "登录用户-令牌生成速率")
    private Integer rate;

    @Schema(description = "游客-桶容量/每日最大次数")
    private Integer guestCapacity;

    @Schema(description = "游客-令牌生成速率")
    private Integer guestRate;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer version;
}
```

### 2. Mapper

文件：`backend/src/main/java/com/testdemo/mapper/RateLimitRuleMapper.java`

```java
@Mapper
public interface RateLimitRuleMapper extends BaseMapper<RateLimitRule> {
}
```

### 3. Service 接口

文件：`backend/src/main/java/com/testdemo/service/RateLimitRuleService.java`

```java
public interface RateLimitRuleService {

    /**
     * 分页查询规则列表
     */
    IPage<RateLimitRule> pageRules(int current, int size, String configKey, Integer status);

    /**
     * 根据 configKey 查询规则
     */
    RateLimitRule getByConfigKey(String configKey);

    /**
     * 新增规则
     */
    boolean addRule(RateLimitRule rule);

    /**
     * 更新规则
     */
    boolean updateRule(RateLimitRule rule);

    /**
     * 删除规则
     */
    boolean deleteRule(Long id);

    /**
     * 根据 ID 查询规则
     */
    RateLimitRule getById(Long id);

    /**
     * 获取启用的规则列表（供缓存预热）
     */
    List<RateLimitRule> listEnabled();
}
```

### 4. Service 实现

文件：`backend/src/main/java/com/testdemo/service/impl/RateLimitRuleServiceImpl.java`

职责：
1. 实现 CRUD 业务逻辑
2. 保存/更新时同步刷新 Redis 缓存
3. 删除时同步清除 Redis 缓存

关键实现要点：
- 使用 MyBatis-Plus 的 `save`/`updateById`/`removeById`
- 操作成功后调用 `RateLimitRuleManager.refreshCache(rule)` 刷新缓存
- 提供 `refreshAllCache()` 方法供手动刷新

### 5. 控制器

文件：`backend/src/main/java/com/testdemo/controller/RateLimitRuleController.java`

```java
@RestController
@RequestMapping("/api/rate-limit/rules")
@RequiredArgsConstructor
@SaCheckPermission("rate-limit:rule:list")
@Tag(name = "限流规则管理", description = "动态限流规则的增删改查")
public class RateLimitRuleController {
    private final RateLimitRuleService rateLimitRuleService;

    @GetMapping("/page")
    @SaCheckPermission("rate-limit:rule:list")
    public Result<?> page(
        @RequestParam(defaultValue = "1") int current,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String configKey,
        @RequestParam(required = false) Integer status
    );

    @GetMapping("/{id}")
    @SaCheckPermission("rate-limit:rule:view")
    public Result<?> getById(@PathVariable Long id);

    @PostMapping
    @SaCheckPermission("rate-limit:rule:add")
    public Result<?> add(@RequestBody @Valid RateLimitRule rule);

    @PutMapping
    @SaCheckPermission("rate-limit:rule:edit")
    public Result<?> update(@RequestBody @Valid RateLimitRule rule);

    @DeleteMapping("/{id}")
    @SaCheckPermission("rate-limit:rule:delete")
    public Result<?> delete(@PathVariable Long id);

    @PostMapping("/refresh-cache")
    @SaCheckPermission("rate-limit:rule:refresh")
    public Result<?> refreshCache();
}
```

### 6. DTO 请求对象

文件：`backend/src/main/java/com/testdemo/dto/RateLimitRuleDTO.java`

```java
@Schema(description = "限流规则请求DTO")
public class RateLimitRuleDTO {
    @Schema(description = "规则配置键，唯一标识，如 sms:send")
    private String configKey;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "限流策略")
    private String limitType;

    @Schema(description = "登录用户-桶容量/每日最大次数")
    private Integer capacity;

    @Schema(description = "登录用户-令牌生成速率")
    private Integer rate;

    @Schema(description = "游客-桶容量/每日最大次数")
    private Integer guestCapacity;

    @Schema(description = "游客-令牌生成速率")
    private Integer guestRate;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
```

### 7. 修改 RateLimitRuleManager

文件：`backend/src/main/java/com/testdemo/config/RateLimitRuleManager.java`

修改要点：
1. 保留 Redis 操作能力
2. 新增 `refreshCache(RateLimitRule rule)` 方法
3. 新增 `refreshAllCache(List<RateLimitRule> rules)` 方法
4. 新增 `clearCache(String configKey)` 方法
5. 新增 `initCache()` 启动时从数据库加载规则

### 8. 修改 DynamicRateLimitAspect

文件：`backend/src/main/java/com/testdemo/aspect/DynamicRateLimitAspect.java`

修改 `around` 方法，增加游客/登录用户差异化限流逻辑：

```java
@Around("@annotation(dynamicRateLimit)")
public Object around(ProceedingJoinPoint point, DynamicRateLimit dynamicRateLimit) throws Throwable {
    // ... 现有 key 构建逻辑 ...

    // 根据是否登录选择不同规则
    int finalCapacity;
    int finalRate;
    boolean isLogin = StpUtil.isLogin();

    if (StringUtils.hasText(dynamicRateLimit.configKey())) {
        RateLimitRule dynamicRule = ruleManager.getRule(dynamicRateLimit.configKey());
        if (dynamicRule != null && dynamicRule.getStatus() == 1) {
            if (isLogin) {
                finalCapacity = dynamicRule.getCapacity() != null
                    ? dynamicRule.getCapacity() : dynamicRateLimit.capacity();
                finalRate = dynamicRule.getRate() != null
                    ? dynamicRule.getRate() : dynamicRateLimit.rate();
            } else {
                // 游客规则
                if (dynamicRule.getGuestCapacity() != null) {
                    finalCapacity = dynamicRule.getGuestCapacity();
                    finalRate = dynamicRule.getGuestRate() != null
                        ? dynamicRule.getGuestRate() : dynamicRule.getRate();
                } else {
                    // 未配置游客规则，降级到正式规则
                    finalCapacity = dynamicRule.getCapacity();
                    finalRate = dynamicRule.getRate();
                }
            }
        } else {
            // 无规则时降级到注解默认值
            finalCapacity = isLogin ? dynamicRateLimit.capacity() : dynamicRateLimit.guestCapacity();
            finalRate = isLogin ? dynamicRateLimit.rate() : dynamicRateLimit.guestRate();
        }
    } else {
        finalCapacity = isLogin ? dynamicRateLimit.capacity() : dynamicRateLimit.guestCapacity();
        finalRate = isLogin ? dynamicRateLimit.rate() : dynamicRateLimit.guestRate();
    }

    // ... 后续限流逻辑不变 ...
}
```

### 9. 修改 DynamicRateLimit 注解

文件：`backend/src/main/java/com/testdemo/annotation/DynamicRateLimit.java`

新增游客相关字段：

```java
public @interface DynamicRateLimit {
    String keyPrefix() default "limit:";

    /** 动态配置的 Key */
    String configKey() default "";

    LimitType limitType() default LimitType.TOKEN_BUCKET;

    /** 登录用户 - 桶容量/每日最大次数 */
    int capacity() default 100;

    /** 登录用户 - 令牌生成速率 */
    int rate() default 10;

    /** 游客 - 桶容量/每日最大次数 */
    int guestCapacity() default 10;

    /** 游客 - 令牌生成速率 */
    int guestRate() default 2;

    String message() default "请求过于频繁，请稍后再试";
}
```

### 10. 启动缓存初始化

文件：`backend/src/main/java/com/testdemo/config/SaTokenConfigure.java` 或新增配置类

实现 `ApplicationRunner` 或 `@PostConstruct`，启动时从数据库加载所有启用规则到 Redis：

```java
@Component
public class RateLimitCacheInitializer implements ApplicationRunner {
    private final RateLimitRuleService rateLimitRuleService;
    private final RateLimitRuleManager rateLimitRuleManager;

    @Override
    public void run(ApplicationArguments args) {
        List<RateLimitRule> rules = rateLimitRuleService.listEnabled();
        rateLimitRuleManager.refreshAllCache(rules);
    }
}
```

---

## 修改文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `schema.sql` | 新增表 | 创建 `sys_rate_limit_rule` 表 |
| `RateLimitRule.java` | 重写 | 适配数据库字段 |
| `RateLimitRuleMapper.java` | 新增 | MyBatis-Plus Mapper |
| `RateLimitRuleService.java` | 新增 | 服务接口 |
| `RateLimitRuleServiceImpl.java` | 新增 | 服务实现 |
| `RateLimitRuleController.java` | 新增 | 管理 API |
| `RateLimitRuleDTO.java` | 新增 | 请求 DTO |
| `RateLimitRuleManager.java` | 修改 | 增加缓存刷新方法 |
| `DynamicRateLimitAspect.java` | 修改 | 支持游客差异化限流 |
| `DynamicRateLimit.java` | 修改 | 增加 guest 字段 |
| `RateLimitCacheInitializer.java` | 新增 | 启动缓存初始化 |

---

## 非目标

本任务不做以下事情：

1. 不修改现有限流切面的核心 Lua 脚本逻辑
2. 不新增前端管理页面
3. 不修改 `RateLimitDemoController` 的现有接口
4. 不调整 `LimitType` 枚举的现有值

---

## 验收标准

任务完成后，必须同时满足以下条件：

1. 数据库存在 `sys_rate_limit_rule` 表，字段完整
2. `RateLimitRule` 实体与数据库表结构一致
3. 提供完整的 CRUD API：
   - `GET /api/rate-limit/rules/page` - 分页查询
   - `GET /api/rate-limit/rules/{id}` - 详情查询
   - `POST /api/rate-limit/rules` - 新增规则
   - `PUT /api/rate-limit/rules` - 更新规则
   - `DELETE /api/rate-limit/rules/{id}` - 删除规则
   - `POST /api/rate-limit/rules/refresh-cache` - 手动刷新缓存
4. 规则新增/更新/删除后自动同步到 Redis
5. 应用启动时自动从数据库加载规则到 Redis
6. 现有 `DynamicRateLimitAspect` 支持游客/登录用户差异化限流
7. 所有管理接口使用 `@SaCheckPermission` 权限控制
8. 后端编译通过

---

## 建议验证步骤

如果允许执行验证，建议按以下顺序检查：

1. 在 `backend` 目录执行 `mvn -q -DskipTests compile`
2. 启动后端服务
3. 使用 admin 账号登录获取 token
4. 调用 `POST /api/rate-limit/rules` 创建规则
5. 调用 `GET /api/rate-limit/rules/page` 查询规则列表
6. 调用限流接口验证规则生效
7. 调用 `DELETE /api/rate-limit/rules/{id}` 删除规则
8. 验证 Redis 中规则已被清除
9. 重启应用，验证启动时规则自动加载

---

## AI 编辑器输出要求

完成实现后，AI 编辑器应明确报告：

1. 修改了哪些文件
2. 新增了哪些文件
3. 数据库变更内容
4. 是否编译通过
5. 若未执行运行验证，必须明确说明：

`Dynamic Rate Limit MySQL Persistence design completed, runtime verification not performed`
