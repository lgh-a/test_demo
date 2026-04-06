package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.testdemo.common.OperationResult;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.RoleCreateRequest;
import com.testdemo.dto.RoleMenuAssignRequest;
import com.testdemo.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Validated
@Tag(name = "Roles", description = "System role management")
public class SysRoleController {

    private final SysRoleService sysRoleService;

    @GetMapping("/list")
    @SaCheckPermission("sys:role:list")
    @Operation(summary = "Get role list", description = "Query all roles")
    @ApiResponse(responseCode = "200", description = "Query successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:role:list permission")
    public Result<?> list() {
        return Result.success(sysRoleService.listRoles());
    }

    @GetMapping("/page")
    @SaCheckPermission("sys:role:list")
    @Operation(summary = "Page roles", description = "Query roles by page")
    @ApiResponse(responseCode = "200", description = "Query successful")
    public Result<?> page(
            @RequestParam(defaultValue = "1")
            @Min(value = 1, message = "current must be greater than 0")
            int current,
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size must be greater than 0")
            @Max(value = 50, message = "size must be less than or equal to 50")
            int size,
            @RequestParam(required = false) String keyword) {
        IPage<?> page = sysRoleService.pageRoles(current, size, keyword);
        return Result.success(page);
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:role:add")
    @Operation(summary = "Create role", description = "Create a new role")
    @ApiResponse(responseCode = "200", description = "Create successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:role:add permission")
    public Result<?> add(@Valid @RequestBody RoleCreateRequest request) {
        OperationResult<Void> result = sysRoleService.createRole(request);
        ensureSuccess(result);
        return Result.success("Role added successfully");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:role:delete")
    @Operation(summary = "Delete role", description = "Delete role and its relations by ID")
    @ApiResponse(responseCode = "200", description = "Delete successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:role:delete permission")
    public Result<?> delete(@Parameter(description = "Role ID") @PathVariable Integer id) {
        OperationResult<Void> result = sysRoleService.deleteRole(id);
        ensureSuccess(result);
        return Result.success("Role deleted successfully");
    }

    @GetMapping("/{id}/menus")
    @SaCheckPermission("sys:role:list")
    @Operation(summary = "Get role menus", description = "Query menu IDs bound to the role")
    @ApiResponse(responseCode = "200", description = "Query successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:role:list permission")
    public Result<?> getRoleMenus(@Parameter(description = "Role ID") @PathVariable Integer id) {
        return Result.success(sysRoleService.getRoleMenuIds(id));
    }

    @PostMapping("/{id}/menus")
    @SaCheckPermission("sys:role:add")
    @Operation(summary = "Assign role menus", description = "Replace role-menu bindings for the given role")
    @ApiResponse(responseCode = "200", description = "Assign successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:role:add permission")
    public Result<?> assignRoleMenus(@Parameter(description = "Role ID") @PathVariable Integer id,
                                     @Valid @RequestBody RoleMenuAssignRequest request) {
        OperationResult<Void> result = sysRoleService.assignRoleMenus(id, request.getMenuIds());
        ensureSuccess(result);
        return Result.success("Permissions assigned successfully");
    }

    private void ensureSuccess(OperationResult<?> result) {
        if (!result.isSuccess()) {
            throw new BusinessException(HttpStatus.valueOf(result.getCode()), result.getCode(), result.getMessage());
        }
    }
}
