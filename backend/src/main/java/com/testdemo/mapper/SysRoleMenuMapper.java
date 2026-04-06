package com.testdemo.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testdemo.entity.SysRoleMenu;
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {

    default java.util.List<SysRoleMenu> selectAll() {
        return selectList(null);
    }

    default java.util.List<SysRoleMenu> selectByRoleId(Integer roleId) {
        return selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
    }

    default java.util.List<SysRoleMenu> selectByMenuId(Integer menuId) {
        return selectList(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, menuId));
    }

    default int insertRelation(SysRoleMenu relation) {
        return insert(relation);
    }

    default int deleteByRoleId(Integer roleId) {
        return delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
    }

    default int deleteByMenuId(Integer menuId) {
        return delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getMenuId, menuId));
    }
}
