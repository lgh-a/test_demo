package com.testdemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminGameSaveRequest {

    @NotBlank(message = "Game name is required")
    @Size(max = 255, message = "Game name must be less than or equal to 255 characters")
    private String name;

    @Size(max = 2000, message = "Description must be less than or equal to 2000 characters")
    private String description;

    @NotBlank(message = "ROM path is required")
    @Size(max = 500, message = "ROM path must be less than or equal to 500 characters")
    private String romUrl;

    @Size(max = 500, message = "Thumbnail path must be less than or equal to 500 characters")
    private String thumbUrl;

    @NotNull(message = "Console is required")
    private Integer consoleId;

    private Integer seriesId;

    @Size(max = 50, message = "Language must be less than or equal to 50 characters")
    private String language;

    @Size(max = 50, message = "Region must be less than or equal to 50 characters")
    private String region;

    @Size(max = 50, message = "Genre must be less than or equal to 50 characters")
    private String genre;

    @NotNull(message = "Status is required")
    @Min(value = 0, message = "Status must be 0 or 1")
    @Max(value = 1, message = "Status must be 0 or 1")
    private Integer status;

    @NotNull(message = "Recommend flag is required")
    @Min(value = 0, message = "Recommend flag must be 0 or 1")
    @Max(value = 1, message = "Recommend flag must be 0 or 1")
    private Integer isRecommend;

    @NotNull(message = "Sort is required")
    @Min(value = 0, message = "Sort must be greater than or equal to 0")
    private Integer sort;
}
