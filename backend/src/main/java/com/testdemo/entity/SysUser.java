package com.testdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser {
    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    private Integer id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonIgnore
    private String password;

    @Schema(description = "状态，1启用0禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
