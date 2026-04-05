package com.testdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserCreateRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    @NotNull(message = "status is required")
    private Integer status;

    @NotNull(message = "roleId is required")
    private Integer roleId;
}
