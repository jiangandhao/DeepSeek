package com.health.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CheckupAppointmentMapper extends BaseMapper<Map<String, Object>> {

    @Select("SELECT * FROM checkup_appointments WHERE user_id = #{userId} ORDER BY appointment_date DESC")
    List<Map<String, Object>> selectByUserId(Long userId);

    @Select("SELECT * FROM checkup_appointments WHERE user_id = #{userId} AND status = #{status}")
    List<Map<String, Object>> selectByUserIdAndStatus(Long userId, String status);
}
