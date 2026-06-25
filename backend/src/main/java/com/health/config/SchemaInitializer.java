package com.health.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS `health_device` (
                  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
                  `user_id`          BIGINT       NOT NULL COMMENT '绑定用户ID',
                  `device_no`        VARCHAR(64)  NOT NULL COMMENT '设备唯一编号',
                  `device_name`      VARCHAR(128) NOT NULL COMMENT '设备名称',
                  `device_type`      VARCHAR(32)  NOT NULL DEFAULT 'CGM' COMMENT 'CGM/血压计/手环等',
                  `status`           VARCHAR(24)  NOT NULL DEFAULT 'ONLINE' COMMENT 'ONLINE/OFFLINE/LOW_BATTERY/UNBOUND',
                  `battery_level`    INT          DEFAULT NULL COMMENT '电量0-100',
                  `signal_strength`  INT          DEFAULT NULL COMMENT '信号0-100',
                  `last_value_mmol`  DECIMAL(4,1) DEFAULT NULL COMMENT '最近血糖上报',
                  `last_measured_at` DATETIME     DEFAULT NULL COMMENT '最近数据时间',
                  `last_payload`     TEXT         DEFAULT NULL COMMENT '最近接口原始负载',
                  `bound_at`         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`id`),
                  UNIQUE KEY `uk_user_device` (`user_id`, `device_no`),
                  KEY `idx_device_status` (`status`, `updated_at`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能健康设备'
                """);
    }
}
