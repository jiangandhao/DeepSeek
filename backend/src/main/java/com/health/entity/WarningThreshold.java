package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("warning_thresholds")
public class WarningThreshold {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String thresholdType;
    private Double thresholdValue;
    private String unit;
}
