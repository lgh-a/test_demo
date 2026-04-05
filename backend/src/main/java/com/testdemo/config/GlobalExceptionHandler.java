package com.testdemo.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.common.exception.RateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<Result<?>> handlerNotLoginException(NotLoginException e) {
        return build(HttpStatus.UNAUTHORIZED, "Not logged in or token expired.");
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<Result<?>> handlerNotPermissionException(NotPermissionException e) {
        return build(HttpStatus.FORBIDDEN, "You don't have permission: " + e.getCode());
    }

    @ExceptionHandler(NotRoleException.class)
    public ResponseEntity<Result<?>> handlerNotRoleException(NotRoleException e) {
        return build(HttpStatus.FORBIDDEN, "You don't have role: " + e.getRole());
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Result<?>> handlerRateLimitException(RateLimitException e) {
        return build(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        return build(HttpStatus.BAD_REQUEST,
                fieldError != null ? fieldError.getDefaultMessage() : "Invalid request parameters");
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<?>> handlerBindException(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        return build(HttpStatus.BAD_REQUEST,
                fieldError != null ? fieldError.getDefaultMessage() : "Invalid request parameters");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<?>> handlerConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Invalid request parameters");
        return build(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class
    })
    public ResponseEntity<Result<?>> handlerBadRequestException(Exception e) {
        return build(HttpStatus.BAD_REQUEST, "Invalid request parameters");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<?>> handlerMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Request method is not supported");
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handlerBusinessException(BusinessException e) {
        return ResponseEntity.status(e.getStatus()).body(Result.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Result<?>> handlerDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return build(HttpStatus.CONFLICT, "Data conflict");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<?>> handlerNoResourceFoundException(NoResourceFoundException e) {
        return build(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handlerException(Exception e) {
        log.error("Unhandled system exception", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "System error");
    }

    private ResponseEntity<Result<?>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Result.error(status.value(), message));
    }
}
