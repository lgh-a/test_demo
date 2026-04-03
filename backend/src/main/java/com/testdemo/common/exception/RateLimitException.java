package com.testdemo.common.exception;

/**
 * 触发限流时抛出的业务异常
 */
public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) {
        super(message);
    }
}