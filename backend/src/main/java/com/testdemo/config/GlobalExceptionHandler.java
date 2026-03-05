package com.testdemo.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.testdemo.common.Result;
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

    @ExceptionHandler(Exception.class)
    public Result<?> handlerException(Exception e) {
        e.printStackTrace();
        return Result.error("System error: " + e.getMessage());
    }
}
