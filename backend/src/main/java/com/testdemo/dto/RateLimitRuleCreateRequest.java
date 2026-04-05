package com.testdemo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RateLimitRuleCreateRequest {

    @NotBlank(message = "configKey is required")
    @Size(max = 100, message = "configKey must not exceed 100 characters")
    private String configKey;

    @Size(max = 100, message = "name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "limitType is required")
    @Size(max = 20, message = "limitType must not exceed 20 characters")
    private String limitType;

    @NotNull(message = "capacity is required")
    @Min(value = 1, message = "capacity must be greater than 0")
    private Integer capacity;

    @NotNull(message = "rate is required")
    @Min(value = 1, message = "rate must be greater than 0")
    private Integer rate;

    @Min(value = 1, message = "guestCapacity must be greater than 0")
    private Integer guestCapacity;

    @Min(value = 1, message = "guestRate must be greater than 0")
    private Integer guestRate;

    @NotNull(message = "status is required")
    @Min(value = 0, message = "status must be 0 or 1")
    @Max(value = 1, message = "status must be 0 or 1")
    private Integer status;
}
