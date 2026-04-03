package com.testdemo.aspect;

import cn.dev33.satoken.stp.StpUtil;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

/**
 * 动态限流切面
 * 实现二级重写逻辑：Redis 动态规则 > 注解硬编码兜底值
 * 支持令牌桶和每日固定窗口两种限流策略
 * 支持游客和登录用户差异化限流
 */
@Aspect
@Component
public class DynamicRateLimitAspect {

    private final StringRedisTemplate stringRedisTemplate;
    private final RateLimitRuleManager ruleManager;

    /** 令牌桶 Lua 脚本 */
    private final DefaultRedisScript<Long> bucketScript;

    /** 每日固定窗口 Lua 脚本 */
    private final DefaultRedisScript<Long> dailyScript;

    /** SpEL 表达式解析器，用于解析 dynamicKey */
    private final ExpressionParser spelParser = new SpelExpressionParser();

    public DynamicRateLimitAspect(StringRedisTemplate stringRedisTemplate, RateLimitRuleManager ruleManager) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.ruleManager = ruleManager;

        // 预加载令牌桶 Lua 脚本，避免请求时动态加载
        this.bucketScript = new DefaultRedisScript<>();
        this.bucketScript.setLocation(new ClassPathResource("lua/rate_limit_bucket.lua"));
        this.bucketScript.setResultType(Long.class);

        // 预加载每日固定窗口 Lua 脚本
        this.dailyScript = new DefaultRedisScript<>();
        this.dailyScript.setLocation(new ClassPathResource("lua/rate_limit_daily.lua"));
        this.dailyScript.setResultType(Long.class);
    }

    /**
     * 核心限流逻辑
     * 二级重写：先取注解默认值，再被 Redis 动态规则覆盖
     * 支持游客和登录用户差异化限流
     */
    @Around("@annotation(dynamicRateLimit)")
    public Object around(ProceedingJoinPoint point, DynamicRateLimit dynamicRateLimit) throws Throwable {
        // 第一步：构建基础 key = keyPrefix + 方法名
        String baseKey = dynamicRateLimit.keyPrefix() + point.getSignature().getName();

        // 第二步：解析 SpEL 表达式获取动态维度（如手机号、用户ID）
        String dynamicPart = parseSpel(dynamicRateLimit.dynamicKey(), point);
        String finalKey = StringUtils.hasText(dynamicPart) ? baseKey + ":" + dynamicPart : baseKey;

        // 第三步：判断是否登录
        boolean isLogin = StpUtil.isLogin();

        // 第四步：获取限流阈值（区分游客和登录用户）
        int finalCapacity;
        int finalRate;

        if (StringUtils.hasText(dynamicRateLimit.configKey())) {
            RateLimitRule dynamicRule = ruleManager.getRule(dynamicRateLimit.configKey());
            if (dynamicRule != null && dynamicRule.getStatus() == 1) {
                if (isLogin) {
                    // 登录用户使用正式规则
                    finalCapacity = dynamicRule.getCapacity() != null
                            ? dynamicRule.getCapacity() : dynamicRateLimit.capacity();
                    finalRate = dynamicRule.getRate() != null
                            ? dynamicRule.getRate() : dynamicRateLimit.rate();
                } else {
                    // 游客使用 guest 规则，若未配置则降级到正式规则
                    if (dynamicRule.getGuestCapacity() != null) {
                        finalCapacity = dynamicRule.getGuestCapacity();
                        finalRate = dynamicRule.getGuestRate() != null
                                ? dynamicRule.getGuestRate() : dynamicRule.getRate();
                    } else {
                        finalCapacity = dynamicRule.getCapacity() != null
                                ? dynamicRule.getCapacity() : dynamicRateLimit.capacity();
                        finalRate = dynamicRule.getRate() != null
                                ? dynamicRule.getRate() : dynamicRateLimit.rate();
                    }
                }
            } else {
                // 无规则时降级到注解默认值
                finalCapacity = isLogin ? dynamicRateLimit.capacity() : dynamicRateLimit.guestCapacity();
                finalRate = isLogin ? dynamicRateLimit.rate() : dynamicRateLimit.guestRate();
            }
        } else {
            // 无 Redis 规则时使用注解硬编码默认值
            finalCapacity = isLogin ? dynamicRateLimit.capacity() : dynamicRateLimit.guestCapacity();
            finalRate = isLogin ? dynamicRateLimit.rate() : dynamicRateLimit.guestRate();
        }

        // 第五步：防御性放行，避免误配置导致全量封禁
        if (finalCapacity <= 0) return point.proceed();

        // 第六步：根据策略类型执行不同的限流逻辑
        Long result;
        if (dynamicRateLimit.limitType() == LimitType.FIXED_WINDOW_DAILY) {
            // 每日固定窗口策略：key 格式为 baseKey:动态维度:日期:v版本号
            String date = LocalDate.now().toString();
            int version = 1;
            if (StringUtils.hasText(dynamicRateLimit.configKey())) {
                RateLimitRule dynamicRule = ruleManager.getRule(dynamicRateLimit.configKey());
                if (dynamicRule != null && dynamicRule.getVersion() != null) {
                    version = dynamicRule.getVersion();
                }
            }
            finalKey = finalKey + ":" + date + ":v" + version;

            // 计算距离当天午夜剩余秒数作为 TTL
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime midnight = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            long secondsToMidnight = ChronoUnit.SECONDS.between(now, midnight);

            // 执行每日限流 Lua 脚本
            result = stringRedisTemplate.execute(dailyScript, Collections.singletonList(finalKey),
                    String.valueOf(finalCapacity), String.valueOf(secondsToMidnight));
        } else {
            // 令牌桶策略：以秒为单位计算令牌补充
            long nowTime = System.currentTimeMillis() / 1000;
            result = stringRedisTemplate.execute(bucketScript, Collections.singletonList(finalKey),
                    String.valueOf(finalCapacity), String.valueOf(finalRate), String.valueOf(nowTime), "1");
        }

        // 第七步：限流触发时抛出业务异常
        if (result == null || result == 0L) {
            throw new RateLimitException(dynamicRateLimit.message());
        }

        // 第八步：未触发限流，执行目标方法
        return point.proceed();
    }

    /**
     * 解析 SpEL 表达式，从方法参数中提取动态维度
     */
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
