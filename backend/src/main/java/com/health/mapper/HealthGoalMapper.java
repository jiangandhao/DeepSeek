package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.HealthGoal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface HealthGoalMapper extends BaseMapper<HealthGoal> {

    @Select("SELECT * FROM health_goals WHERE user_id = #{userId}")
    List<HealthGoal> selectByUserId(@Param("userId") Long userId);
}
