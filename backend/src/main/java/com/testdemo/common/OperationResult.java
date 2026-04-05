package com.testdemo.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OperationResult<T> {

    private final boolean success;
    private final int code;
    private final String message;
    private final T data;

    public static <T> OperationResult<T> success(T data) {
        return new OperationResult<>(true, 200, null, data);
    }

    public static OperationResult<Void> success() {
        return new OperationResult<>(true, 200, null, null);
    }

    public static <T> OperationResult<T> failure(String message) {
        return failure(409, message);
    }

    public static <T> OperationResult<T> failure(int code, String message) {
        return new OperationResult<>(false, code, message, null);
    }
}
