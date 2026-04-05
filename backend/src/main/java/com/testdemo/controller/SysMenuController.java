package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.testdemo.common.OperationResult;
import com.testdemo.common.Result;
import com.testdemo.common.exception.BusinessException;
import com.testdemo.dto.MenuSaveRequest;
import com.testdemo.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Validated
@Tag(name = "Menus", description = "System menu management")
public class SysMenuController {

    private final SysMenuService sysMenuService;

    @GetMapping("/list")
    @SaCheckPermission("sys:menu:list")
    @Operation(summary = "Get menu list", description = "Query all menus in flat structure")
    @ApiResponse(responseCode = "200", description = "Query successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:menu:list permission")
    public Result<?> list() {
        return Result.success(sysMenuService.listMenus());
    }

    @GetMapping("/tree")
    @SaCheckPermission("sys:menu:list")
    @Operation(summary = "Get menu tree", description = "Query all menus in tree structure")
    @ApiResponse(responseCode = "200", description = "Query successful")
    public Result<?> tree() {
        return Result.success(sysMenuService.treeMenus());
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:menu:add")
    @Operation(summary = "Create menu", description = "Create a new menu")
    @ApiResponse(responseCode = "200", description = "Create successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:menu:add permission")
    public Result<?> add(@Valid @RequestBody MenuSaveRequest request) {
        OperationResult<Void> result = sysMenuService.createMenu(request);
        ensureSuccess(result);
        return Result.success("Menu added successfully");
    }

    @PutMapping("/{id}")
    @SaCheckPermission("sys:menu:update")
    @Operation(summary = "Update menu", description = "Update menu by ID")
    @ApiResponse(responseCode = "200", description = "Update successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:menu:update permission")
    public Result<?> update(@Parameter(description = "Menu ID") @PathVariable Integer id,
                            @Valid @RequestBody MenuSaveRequest request) {
        OperationResult<Void> result = sysMenuService.updateMenu(id, request);
        ensureSuccess(result);
        return Result.success("Menu updated successfully");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:menu:delete")
    @Operation(summary = "Delete menu", description = "Delete menu by ID")
    @ApiResponse(responseCode = "200", description = "Delete successful")
    @ApiResponse(responseCode = "401", description = "Not logged in")
    @ApiResponse(responseCode = "403", description = "Missing sys:menu:delete permission")
    public Result<?> delete(@Parameter(description = "Menu ID") @PathVariable Integer id) {
        OperationResult<Void> result = sysMenuService.deleteMenu(id);
        ensureSuccess(result);
        return Result.success("Menu deleted successfully");
    }

    private void ensureSuccess(OperationResult<?> result) {
        if (!result.isSuccess()) {
            throw new BusinessException(HttpStatus.valueOf(result.getCode()), result.getCode(), result.getMessage());
        }
    }
}
