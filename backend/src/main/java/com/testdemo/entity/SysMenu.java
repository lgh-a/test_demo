package com.testdemo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer parentId;
    private String name;
    private String perms;
    private Integer type;
    private Integer sort;
    private String icon;
    private String path;
    
    @TableField(exist = false)
    private List<SysMenu> children;
}
