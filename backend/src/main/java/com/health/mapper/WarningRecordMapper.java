package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.entity.WarningRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface WarningRecordMapper extends BaseMapper<WarningRecord> {

    @Select("SELECT * FROM warning_records WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<WarningRecord> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM warning_records WHERE user_id = #{userId} AND warning_level = #{level} ORDER BY create_time DESC")
    List<WarningRecord> selectByUserIdAndLevel(@Param("userId") Long userId, @Param("level") String level);

    @Update("UPDATE warning_records SET is_read = 1 WHERE id = #{id}")
    int markRead(@Param("id") Long id);

    @Update("UPDATE warning_records SET is_read = 1 WHERE user_id = #{userId}")
    int markAllRead(@Param("userId") Long userId);

    @Update("UPDATE warning_records SET is_dismissed = 1 WHERE id = #{id}")
    int dismiss(@Param("id") Long id);
}
