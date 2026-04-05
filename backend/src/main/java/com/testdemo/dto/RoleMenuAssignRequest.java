package com.testdemo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RoleMenuAssignRequest {

    @NotNull(message = "menuIds is required")
    @Size(max = 500, message = "menuIds must not exceed 500 items")
    private List<@NotNull(message = "menuId must not be null") Integer> menuIds;
}
