package com.health.mapper;

import com.health.entity.CheckupReport;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CheckupReportMapper extends BaseMapper<CheckupReport> {

    @Select("SELECT * FROM checkup_reports WHERE user_id = #{userId} ORDER BY checkup_date DESC")
    List<CheckupReport> selectByUserId(Long userId);
}
