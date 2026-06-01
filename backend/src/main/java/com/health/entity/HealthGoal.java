package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("health_goals")
public class HealthGoal {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String goalName;
    private Double targetValue;
    private Double currentValue;
    private Integer progress;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
