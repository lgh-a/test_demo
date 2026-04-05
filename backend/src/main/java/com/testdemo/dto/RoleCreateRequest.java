package com.testdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleCreateRequest {

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must not exceed 50 characters")
    private String name;

    @Size(max = 255, message = "remark must not exceed 255 characters")
    private String remark;
}
