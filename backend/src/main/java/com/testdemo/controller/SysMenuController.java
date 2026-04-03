package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.testdemo.common.Result;
import com.testdemo.entity.SysMenu;
import com.testdemo.mapper.SysMenuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "菜单管理", description = "系统菜单CRUD操作，需要相应权限")
public class SysMenuController {

    private final SysMenuMapper sysMenuMapper;

    @GetMapping("/list")
    @SaCheckPermission("sys:menu:list")
    @Operation(summary = "获取菜单列表", description = "查询所有系统菜单，需要sys:menu:list权限")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:menu:list权限")
    public Result<?> list() {
        return Result.success(sysMenuMapper.selectList(null));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:menu:add")
    @Operation(summary = "添加菜单", description = "新增系统菜单，需要sys:menu:add权限")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:menu:add权限")
    public Result<?> add(@RequestBody SysMenu menu) {
        sysMenuMapper.insert(menu);
        return Result.success("Menu added successfully");
    }

    @PutMapping("/{id}")
    @SaCheckPermission("sys:menu:update")
    @Operation(summary = "更新菜单", description = "根据ID更新菜单信息，需要sys:menu:update权限")
    @ApiResponse(responseCode = "200", description = "更新成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:menu:update权限")
    public Result<?> update(@Parameter(description = "菜单ID") @PathVariable Integer id, @RequestBody SysMenu menu) {
        menu.setId(id);
        sysMenuMapper.updateById(menu);
        return Result.success("Menu updated successfully");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:menu:delete")
    @Operation(summary = "删除菜单", description = "根据ID删除菜单，需要sys:menu:delete权限")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:menu:delete权限")
    public Result<?> delete(@Parameter(description = "菜单ID") @PathVariable Integer id) {
        sysMenuMapper.deleteById(id);
        return Result.success("Menu deleted successfully");
    }
}
