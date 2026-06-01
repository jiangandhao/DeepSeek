package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("checkup_appointments")
public class CheckupAppointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String hospitalName;
    private LocalDateTime appointmentDate;
    private String appointmentTime;
    private String checkupItems;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
