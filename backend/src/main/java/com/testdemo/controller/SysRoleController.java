package com.testdemo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.testdemo.common.Result;
import com.testdemo.entity.SysRole;
import com.testdemo.entity.SysRoleMenu;
import com.testdemo.mapper.SysRoleMapper;
import com.testdemo.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SysRoleController {

    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;

    @GetMapping("/list")
    @SaCheckPermission("sys:role:list")
    public Result<?> list() {
        return Result.success(sysRoleMapper.selectList(null));
    }

    @PostMapping("/add")
    @SaCheckPermission("sys:role:add")
    public Result<?> add(@RequestBody SysRole role) {
        sysRoleMapper.insert(role);
        return Result.success("Role added successfully");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:role:delete")
    @Transactional
    public Result<?> delete(@PathVariable Integer id) {
        sysRoleMapper.deleteById(id);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        return Result.success("Role deleted successfully");
    }

    @GetMapping("/{id}/menus")
    @SaCheckPermission("sys:role:list")
    public Result<?> getRoleMenus(@PathVariable Integer id) {
        List<SysRoleMenu> rms = sysRoleMenuMapper
                .selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, id));
        List<Integer> menuIds = rms.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return Result.success(menuIds);
    }

    @PostMapping("/{id}/menus")
    @SaCheckPermission("sys:role:add")
    @Transactional
    public Result<?> assignRoleMenus(@PathVariable Integer id, @RequestBody List<Integer> menuIds) {
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
