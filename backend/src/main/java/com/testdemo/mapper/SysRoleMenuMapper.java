package com.testdemo.mapper;

import com.testdemo.entity.SysRoleMenu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMenuMapper {

    @Select("SELECT role_id, menu_id FROM sys_role_menu")
    List<SysRoleMenu> selectAll();

    @Select("SELECT role_id, menu_id FROM sys_role_menu WHERE role_id = #{roleId}")
    List<SysRoleMenu> selectByRoleId(@Param("roleId") Integer roleId);

    @Select("SELECT role_id, menu_id FROM sys_role_menu WHERE menu_id = #{menuId}")
    List<SysRoleMenu> selectByMenuId(@Param("menuId") Integer menuId);

    @Insert("INSERT INTO sys_role_menu(role_id, menu_id) VALUES(#{roleId}, #{menuId})")
    int insertRelation(SysRoleMenu relation);

    @Delete("DELETE FROM sys_role_menu WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Integer roleId);

    @Delete("DELETE FROM sys_role_menu WHERE menu_id = #{menuId}")
    int deleteByMenuId(@Param("menuId") Integer menuId);
}
