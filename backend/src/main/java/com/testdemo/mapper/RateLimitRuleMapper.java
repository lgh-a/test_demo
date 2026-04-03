package com.testdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.testdemo.entity.RateLimitRule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RateLimitRuleMapper extends BaseMapper<RateLimitRule> {
}
