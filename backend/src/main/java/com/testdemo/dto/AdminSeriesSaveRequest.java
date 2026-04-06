package com.testdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminSeriesSaveRequest {

    @NotBlank(message = "Series name is required")
    @Size(max = 100, message = "Series name must be less than or equal to 100 characters")
    private String name;

    @Size(max = 255, message = "Icon URL must be less than or equal to 255 characters")
    private String iconUrl;

    @Size(max = 2000, message = "Description must be less than or equal to 2000 characters")
    private String description;
}
