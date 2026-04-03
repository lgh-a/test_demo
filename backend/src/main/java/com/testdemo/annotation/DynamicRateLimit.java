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

    /** 动态配置的 Key (可选)。
     * 配置此项后，优先从 Redis 获取规则。若未配置或获取失败，则降级使用下方 capacity/rate 默认值。
     */
    String configKey() default "";

    /** 限流策略，默认为令牌桶高并发防刷 */
    LimitType limitType() default LimitType.TOKEN_BUCKET;

    /** 登录用户 - 桶容量 或 每日最大允许次数 */
    int capacity() default 100;

    /** 登录用户 - 令牌生成速率（个/秒） */
    int rate() default 10;

    /** 游客 - 桶容量 或 每日最大允许次数 */
    int guestCapacity() default 10;

    /** 游客 - 令牌生成速率（个/秒） */
    int guestRate() default 2;

    String message() default "请求过于频繁，请稍后再试";
}
