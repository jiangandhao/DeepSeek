package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.WarningThreshold;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface WarningThresholdMapper extends BaseMapper<WarningThreshold> {

    @Select("SELECT * FROM warning_thresholds WHERE user_id = #{userId}")
    List<WarningThreshold> selectByUserId(@Param("userId") Long userId);
}
