package com.testdemo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "限流规则请求DTO")
public class RateLimitRuleDTO {
    @Schema(description = "规则配置键，唯一标识，如 sms:send")
    private String configKey;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "限流策略：TOKEN_BUCKET / FLEXIBLE")
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
}
