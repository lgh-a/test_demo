package com.testdemo.mapper;

import com.testdemo.entity.SysUserRole;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysUserRoleMapper {

    @Select("SELECT user_id, role_id FROM sys_user_role")
    List<SysUserRole> selectAll();

    @Select("SELECT user_id, role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<SysUserRole> selectByUserId(@Param("userId") Integer userId);

    @Select("SELECT user_id, role_id FROM sys_user_role WHERE role_id = #{roleId}")
    List<SysUserRole> selectByRoleId(@Param("roleId") Integer roleId);

    @Insert("INSERT INTO sys_user_role(user_id, role_id) VALUES(#{userId}, #{roleId})")
    int insertRelation(SysUserRole relation);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Integer userId);

    @Delete("DELETE FROM sys_user_role WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Integer roleId);
}
