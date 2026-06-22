package com.health.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("health_report")
public class HealthReport {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String filename;
    private String reportType;
    private String extractionMethod;
    private String riskLevel;
    private String summary;
    private Integer abnormalCount;
    private String structuredJson;
    private LocalDateTime createdAt;
}
