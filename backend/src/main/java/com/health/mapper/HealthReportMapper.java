package com.health.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.HealthReport;

@Mapper
public interface HealthReportMapper extends BaseMapper<HealthReport> {
}
