package com.testdemo.config;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.testdemo.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysMenuMapper sysMenuMapper;

    /**
     * 获取权限列表 (加入 Sa-Token Session 缓存)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 获取当前账号的 Session (存放在 Redis 中)
        SaSession session = StpUtil.getSessionByLoginId(loginId);

        // 尝试从 Session 中获取权限列表
        List<String> permissionList = (List<String>) session.get("Permission_List");

        // 如果缓存中没有，则查询数据库，并写入缓存
        if (permissionList == null) {
            permissionList = sysMenuMapper.selectPermsByUserId(Integer.parseInt(loginId.toString()));
            session.set("Permission_List", permissionList);
        }

        return permissionList;
    }

    /**
     * 获取角色列表 (加入 Sa-Token Session 缓存)
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<String> getRoleList(Object loginId, String loginType) {
        // 获取当前账号的 Session (存放在 Redis 中)
        SaSession session = StpUtil.getSessionByLoginId(loginId);

        // 尝试从 Session 中获取角色列表
        List<String> roleList = (List<String>) session.get("Role_List");

        // 如果缓存中没有，则查询数据库，并写入缓存
        if (roleList == null) {
            roleList = sysMenuMapper.selectRolesByUserId(Integer.parseInt(loginId.toString()));
            session.set("Role_List", roleList);
        }

        return roleList;
    }
}