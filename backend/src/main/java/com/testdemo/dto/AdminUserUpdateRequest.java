package com.testdemo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserUpdateRequest {

    @NotNull(message = "status is required")
    private Integer status;

    @NotNull(message = "roleId is required")
    private Integer roleId;
}
