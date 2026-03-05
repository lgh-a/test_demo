package com.testdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testdemo.entity.SysMenu;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    @Select("SELECT m.perms FROM sys_menu m " +
            "JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms != ''")
    List<String> selectPermsByUserId(Integer userId);

    @Select("SELECT r.name FROM sys_role r " +
            "JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId}")
    List<String> selectRolesByUserId(Integer userId);
}
