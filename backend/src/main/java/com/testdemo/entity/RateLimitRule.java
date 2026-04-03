package com.testdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_rate_limit_rule")
@Schema(description = "动态限流规则")
public class RateLimitRule {
    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "规则配置键，唯一标识")
    private String configKey;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "限流策略：TOKEN_BUCKET-令牌桶，FLEXIBLE-固定窗口每日")
    private String limitType;

    @Schema(description = "登录用户-桶容量/每日最大次数")
    private Integer capacity;

    @Schema(description = "登录用户-令牌生成速率")
    private Integer rate;

    @Schema(description = "游客-桶容量/每日最大次数")
    private Integer guestCapacity;

    @Schema(description = "游客-令牌生成速率")
    private Integer guestRate;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "版本号")
    private Integer version;
}
