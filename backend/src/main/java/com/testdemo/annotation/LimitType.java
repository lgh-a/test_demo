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