package com.testdemo.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testdemo.entity.SysUserRole;
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    default java.util.List<SysUserRole> selectAll() {
        return selectList(null);
    }

    default java.util.List<SysUserRole> selectByUserId(Integer userId) {
        return selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }

    default java.util.List<SysUserRole> selectByRoleId(Integer roleId) {
        return selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
    }

    default int insertRelation(SysUserRole relation) {
        return insert(relation);
    }

    default int deleteByUserId(Integer userId) {
        return delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
    }

    default int deleteByRoleId(Integer roleId) {
        return delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
    }
}
