package com.testdemo.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("game_series")
public class GameSeries {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String iconUrl;
    private String description;
}
