package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.testdemo.common.OperationResult;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.AdminUserCreateRequest;
import com.testdemo.dto.AdminUserUpdateRequest;
import com.testdemo.service.AdminUserService;
import com.testdemo.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Admin user management")
public class SysUserController {

    private final AdminUserService adminUserService;
    private final SysUserService sysUserService;

    @GetMapping("/list")
    @SaCheckPermission("sys:user:list")
    @Operation(summary = "Get user list")
    @ApiResponse(responseCode = "200", description = "Query successful")
    public Result<?> list() {
        return Result.success(adminUserService.listUsers());
    }

    @PostMapping
    @SaCheckPermission("sys:user:add")
    @Operation(summary = "Create user")
    @ApiResponse(responseCode = "200", description = "Create successful")
    public Result<?> create(@Valid @RequestBody AdminUserCreateRequest request) {
        ensureSuccess(adminUserService.createUser(request));
        return Result.success("User created successfully");
    }

    @PutMapping("/{id}")
    @SaCheckPermission("sys:user:update")
    @Operation(summary = "Update user")
    @ApiResponse(responseCode = "200", description = "Update successful")
    public Result<?> update(@PathVariable Integer id, @Valid @RequestBody AdminUserUpdateRequest request) {
        ensureSuccess(adminUserService.updateUser(id, request));
        return Result.success("User updated successfully");
    }

    @PostMapping("/{id}/reset-password")
    @SaCheckPermission("sys:user:reset-password")
    @Operation(summary = "Reset user password")
    @ApiResponse(responseCode = "200", description = "Reset successful")
    public Result<?> resetPassword(@PathVariable Integer id) {
        ensureSuccess(adminUserService.resetPassword(id));
        return Result.success("Password reset to 123456");
    }

    private void ensureSuccess(OperationResult<?> result) {
        if (!result.isSuccess()) {
            throw new BusinessException(HttpStatus.valueOf(result.getCode()), result.getCode(), result.getMessage());
        }
    }
}
