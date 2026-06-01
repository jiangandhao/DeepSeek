package com.health.mapper;

import com.health.entity.HealthProfile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HealthProfileMapper extends BaseMapper<HealthProfile> {

    @Select("SELECT * FROM health_profiles WHERE user_id = #{userId}")
    HealthProfile selectByUserId(Long userId);
}
