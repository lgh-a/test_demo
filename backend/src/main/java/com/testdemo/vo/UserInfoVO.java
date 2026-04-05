package com.testdemo.vo;

import com.testdemo.entity.SysUser;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserInfoVO {
    private Integer id;
    private String username;
    private Integer status;
    private LocalDateTime createTime;

    public static UserInfoVO from(SysUser user) {
        if (user == null) {
            return null;
        }
        return new UserInfoVO(
                user.getId(),
                user.getUsername(),
                user.getStatus(),
                user.getCreateTime()
        );
    }
}
