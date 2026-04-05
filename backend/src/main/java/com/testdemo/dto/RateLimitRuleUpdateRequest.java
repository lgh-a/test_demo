package com.testdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RateLimitRuleUpdateRequest extends RateLimitRuleCreateRequest {

    @NotNull(message = "id is required")
    private Long id;
}
