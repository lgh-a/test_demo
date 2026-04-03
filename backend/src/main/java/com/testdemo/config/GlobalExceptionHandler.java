package com.testdemo.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.testdemo.common.Result;
import com.testdemo.common.exception.RateLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handlerNotLoginException(NotLoginException e) {
        return Result.error("Not logged in or token expired.");
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handlerNotPermissionException(NotPermissionException e) {
        return Result.error("You don't have permission: " + e.getCode());
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> handlerNotRoleException(NotRoleException e) {
        return Result.error("You don't have role: " + e.getRole());
    }

    @ExceptionHandler(RateLimitException.class)
    public Result<?> handlerRateLimitException(RateLimitException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<?>> handlerNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error("Static resource not found: " + e.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handlerException(Exception e) {
        e.printStackTrace();
        return Result.error("System error: " + e.getMessage());
    }
}
