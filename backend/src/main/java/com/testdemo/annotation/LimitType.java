package com.testdemo.annotation;

/**
 * 限流策略枚举
 */
public enum LimitType {
    /** 令牌桶：适合 QPS 级别的防刷与系统保护 */
    TOKEN_BUCKET,
    /** 固定窗口（按天）：适合业务上的每日配额限制 */
    FIXED_WINDOW_DAILY;

    public static LimitType resolve(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if ("FLEXIBLE".equalsIgnoreCase(normalized)) {
            return FIXED_WINDOW_DAILY;
        }

        for (LimitType type : values()) {
            if (type.name().equalsIgnoreCase(normalized)) {
                return type;
            }
        }
        return null;
    }
}
