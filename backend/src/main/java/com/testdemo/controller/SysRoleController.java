package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.common.Result;
import com.testdemo.entity.SysRole;
import com.testdemo.entity.SysRoleMenu;
import com.testdemo.mapper.SysRoleMapper;
import com.testdemo.mapper.SysRoleMenuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "角色管理", description = "系统角色CRUD和菜单权限分配，需要相应权限")
public class SysRoleController {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @GetMapping("/list")
    @SaCheckPermission("sys:role:list")
    @Operation(summary = "获取角色列表", description = "查询所有系统角色，需要sys:role:list权限")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:role:list权限")
    public Result<?> list() {
        return Result.success(sysRoleMapper.selectList(null));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:role:add")
    @Operation(summary = "添加角色", description = "新增系统角色，需要sys:role:add权限")
    @ApiResponse(responseCode = "200", description = "添加成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:role:add权限")
    public Result<?> add(@RequestBody SysRole role) {
        sysRoleMapper.insert(role);
        return Result.success("Role added successfully");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:role:delete")
    @Operation(summary = "删除角色", description = "根据ID删除角色及其菜单关联，需要sys:role:delete权限")
    @ApiResponse(responseCode = "200", description = "删除成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:role:delete权限")
    @Transactional
    public Result<?> delete(@Parameter(description = "角色ID") @PathVariable Integer id) {
        sysRoleMapper.deleteById(id);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return Result.success("Role deleted successfully");
    }

    @GetMapping("/{id}/menus")
    @SaCheckPermission("sys:role:list")
    @Operation(summary = "获取角色菜单", description = "获取指定角色关联的菜单ID列表，需要sys:role:list权限")
    @ApiResponse(responseCode = "200", description = "获取成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:role:list权限")
    public Result<?> getRoleMenus(@Parameter(description = "角色ID") @PathVariable Integer id) {
        List<SysRoleMenu> rms = sysRoleMenuMapper
                .selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        List<Integer> menuIds = rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return Result.success(menuIds);
    }

    @PostMapping("/{id}/menus")
    @SaCheckPermission("sys:role:add")
    @Operation(summary = "分配角色菜单", description = "为指定角色分配菜单权限，需要sys:role:add权限")
    @ApiResponse(responseCode = "200", description = "分配成功")
    @ApiResponse(responseCode = "401", description = "未登录")
    @ApiResponse(responseCode = "403", description = "无sys:role:add权限")
    @Transactional
    public Result<?> assignRoleMenus(@Parameter(description = "角色ID") @PathVariable Integer id, @RequestBody List<Integer> menuIds) {
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        for (Integer menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(id);
            rm.setMenuId(menuId);
            sysRoleMenuMapper.insert(rm);
        }
        return Result.success("Permissions assigned successfully");
    }
}
