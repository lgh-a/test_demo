package com.testdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_favorite")
public class UserFavorite {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer gameId;
    private LocalDateTime createTime;
}
