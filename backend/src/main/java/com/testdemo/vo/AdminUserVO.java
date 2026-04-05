package com.testdemo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AdminUserVO {
    private Integer id;
    private String username;
    private Integer status;
    private LocalDateTime createTime;
    private Integer roleId;
    private String roleName;
}
