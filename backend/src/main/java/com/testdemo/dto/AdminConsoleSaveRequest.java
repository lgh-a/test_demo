package com.testdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminConsoleSaveRequest {

    @NotBlank(message = "Console name is required")
    @Size(max = 50, message = "Console name must be less than or equal to 50 characters")
    private String name;

    @NotBlank(message = "Console full name is required")
    @Size(max = 100, message = "Console full name must be less than or equal to 100 characters")
    private String fullName;

    @Size(max = 255, message = "Icon URL must be less than or equal to 255 characters")
    private String iconUrl;
}
