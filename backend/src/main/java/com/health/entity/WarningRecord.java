package com.health.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("warning_records")
public class WarningRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String warningType;
    private String warningLevel;
    private String title;
    private String content;
    private Integer isRead;
    private Integer isDismissed;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
