package com.testdemo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuSaveRequest {

    @Min(value = 0, message = "parentId must be greater than or equal to 0")
    private Integer parentId;

    @NotBlank(message = "name is required")
    @Size(max = 50, message = "name must not exceed 50 characters")
    private String name;

    @Size(max = 100, message = "perms must not exceed 100 characters")
    private String perms;

    @NotNull(message = "type is required")
    @Min(value = 1, message = "type must be between 1 and 3")
    @Max(value = 3, message = "type must be between 1 and 3")
    private Integer type;

    @Min(value = 0, message = "sort must be greater than or equal to 0")
    private Integer sort;

    @Size(max = 50, message = "icon must not exceed 50 characters")
    private String icon;

    @Size(max = 200, message = "path must not exceed 200 characters")
    private String path;
}
