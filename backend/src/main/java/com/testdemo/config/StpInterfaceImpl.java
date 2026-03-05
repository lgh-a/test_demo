package com.testdemo.config;

import cn.dev33.satoken.stp.StpInterface;
import com.testdemo.mapper.SysMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final SysMenuMapper sysMenuMapper;

    /**
     * Get permission list
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return sysMenuMapper.selectPermsByUserId(Integer.parseInt(loginId.toString()));
    }

    /**
     * Get role list
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return sysMenuMapper.selectRolesByUserId(Integer.parseInt(loginId.toString()));
    }
}
