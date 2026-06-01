package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.ImageAnalysisRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ImageAnalysisRecordMapper extends BaseMapper<ImageAnalysisRecord> {

    @Select("SELECT * FROM image_analysis_records WHERE task_id = #{taskId}")
    ImageAnalysisRecord selectByTaskId(@Param("taskId") String taskId);
}
