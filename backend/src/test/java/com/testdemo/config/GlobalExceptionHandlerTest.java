package com.testdemo.config;

import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.common.exception.RateLimitException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldMapBusinessExceptionToMatchingStatusAndCode() {
        ResponseEntity<Result<?>> response = handler.handlerBusinessException(
                new BusinessException(HttpStatus.CONFLICT, "Role name already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(409);
        assertThat(response.getBody().getMsg()).isEqualTo("Role name already exists");
    }

    @Test
    void shouldMapRateLimitExceptionTo429() {
        ResponseEntity<Result<?>> response = handler.handlerRateLimitException(
                new RateLimitException("Too many requests"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(429);
        assertThat(response.getBody().getMsg()).isEqualTo("Too many requests");
    }
}
