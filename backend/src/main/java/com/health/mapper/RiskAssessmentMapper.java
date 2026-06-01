package com.health.mapper;

import com.health.entity.RiskAssessment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RiskAssessmentMapper extends BaseMapper<RiskAssessment> {

    @Select("SELECT * FROM risk_assessments WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<RiskAssessment> selectByUserId(Long userId);
}
