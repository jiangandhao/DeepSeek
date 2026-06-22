-- 基于 DeepSeek 的全流程健康管理系统 — 数据库初始化脚本
-- 字符集统一 utf8mb4,引擎 InnoDB

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(64)  NOT NULL COMMENT '登录用户名',
  `password`    VARCHAR(128) NOT NULL COMMENT 'BCrypt 加密密码',
  `nickname`    VARCHAR(64)  DEFAULT NULL COMMENT '昵称',
  `phone`       VARCHAR(256) DEFAULT NULL COMMENT '手机号(加密存储)',
  `gender`      TINYINT      DEFAULT 0 COMMENT '0未知 1男 2女',
  `birth_date`  DATE         DEFAULT NULL COMMENT '出生日期',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 健康档案(为后续健管师/风险预警预留)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `health_profile` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT      NOT NULL COMMENT '用户ID',
  `height_cm`      DECIMAL(5,1) DEFAULT NULL COMMENT '身高cm',
  `weight_kg`      DECIMAL(5,1) DEFAULT NULL COMMENT '体重kg',
  `family_history` VARCHAR(512) DEFAULT NULL COMMENT '家族病史',
  `chronic`        VARCHAR(512) DEFAULT NULL COMMENT '基础/慢性疾病',
  `diabetes_type`  TINYINT      DEFAULT 0 COMMENT '0无 1型 2型 3妊娠',
  `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健康档案';

-- ----------------------------
-- 血糖记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `glucose_record` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL,
  `value_mmol`   DECIMAL(4,1) NOT NULL COMMENT '血糖值 mmol/L',
  `period`       VARCHAR(16)  NOT NULL COMMENT '测量时段:FASTING空腹/BEFORE_MEAL餐前/AFTER_MEAL餐后/BEDTIME睡前/RANDOM随机',
  `measured_at`  DATETIME     NOT NULL COMMENT '测量时间',
  `note`         VARCHAR(255) DEFAULT NULL,
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `measured_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血糖记录';

-- ----------------------------
-- 饮食记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `diet_record` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `meal_type`   VARCHAR(16)  NOT NULL COMMENT 'BREAKFAST/LUNCH/DINNER/SNACK',
  `food`        VARCHAR(512) NOT NULL COMMENT '食物描述',
  `calories`    INT          DEFAULT NULL COMMENT '估算热量 kcal',
  `carbs_g`     DECIMAL(6,1) DEFAULT NULL COMMENT '碳水克数',
  `eaten_at`    DATETIME     NOT NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `eaten_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录';

-- ----------------------------
-- 运动记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `exercise_record` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL,
  `type`         VARCHAR(64)  NOT NULL COMMENT '运动类型:步行/跑步/游泳等',
  `duration_min` INT          NOT NULL COMMENT '时长(分钟)',
  `intensity`    VARCHAR(16)  NOT NULL COMMENT 'LOW/MEDIUM/HIGH',
  `calories`     INT          DEFAULT NULL COMMENT '消耗热量 kcal',
  `done_at`      DATETIME     NOT NULL,
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `done_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动记录';

-- ----------------------------
-- AI 生成的健康方案
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_advice` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `type`        VARCHAR(32) NOT NULL COMMENT 'DIET饮食/EXERCISE运动/COMPREHENSIVE综合',
  `content`     TEXT        NOT NULL COMMENT 'AI生成内容(Markdown)',
  `context`     TEXT        DEFAULT NULL COMMENT '生成时使用的数据摘要/检索片段',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI健康方案';

-- ----------------------------
-- 预警记录
-- ----------------------------
CREATE TABLE IF NOT EXISTS `alert` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `category`    VARCHAR(32) NOT NULL COMMENT 'GLUCOSE_HIGH/GLUCOSE_LOW/FLUCTUATION/RISK',
  `level`       VARCHAR(16) NOT NULL COMMENT 'LOW/MEDIUM/HIGH',
  `message`     VARCHAR(512) NOT NULL,
  `is_read`     TINYINT     NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录';

-- ----------------------------
-- AI 对话历史(多轮对话)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT      NOT NULL,
  `conversation`  VARCHAR(64) NOT NULL COMMENT '会话ID',
  `role`          VARCHAR(16) NOT NULL COMMENT 'user/assistant',
  `content`       TEXT        NOT NULL,
  `created_at`    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_conv` (`user_id`, `conversation`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史';

-- ----------------------------
-- 体检中心(智能预约,阶段4)
-- ----------------------------
CREATE TABLE IF NOT EXISTS `exam_center` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `name`       VARCHAR(128) NOT NULL COMMENT '机构名称',
  `address`    VARCHAR(255) NOT NULL,
  `lat`        DECIMAL(9,6) NOT NULL COMMENT '纬度',
  `lng`        DECIMAL(9,6) NOT NULL COMMENT '经度',
  `busyness`   INT          NOT NULL DEFAULT 50 COMMENT '繁忙度0-100',
  `packages`   VARCHAR(512) DEFAULT NULL COMMENT '体检套餐(逗号分隔)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检中心';

INSERT INTO `exam_center` (`name`,`address`,`lat`,`lng`,`busyness`,`packages`) VALUES
('市第一人民医院体检中心','市中心人民路1号',31.230400,121.473700,80,'基础套餐,深度套餐,肿瘤筛查'),
('美年大健康(浦东店)','浦东新区世纪大道100号',31.221500,121.544300,45,'基础套餐,白领套餐,心脑血管'),
('慈铭体检(徐汇店)','徐汇区漕溪北路88号',31.190200,121.437600,60,'基础套餐,深度套餐,糖尿病专项'),
('瑞慈体检(虹口店)','虹口区四川北路500号',31.265800,121.487900,30,'基础套餐,中老年套餐,糖尿病专项'),
('社区健康服务中心','长宁区天山路300号',31.210900,121.401200,20,'基础套餐,慢病随访');

-- ----------------------------
-- 体检预约
-- ----------------------------
CREATE TABLE IF NOT EXISTS `appointment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `center_id`   BIGINT       NOT NULL,
  `center_name` VARCHAR(128) NOT NULL,
  `exam_date`   DATE         NOT NULL,
  `package`     VARCHAR(128) DEFAULT NULL,
  `status`      VARCHAR(16)  NOT NULL DEFAULT 'BOOKED' COMMENT 'BOOKED/DONE/CANCELLED',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检预约';

-- ----------------------------
-- 体检报告归档与结构化分析
-- ----------------------------
CREATE TABLE IF NOT EXISTS `health_report` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`           BIGINT       NOT NULL,
  `filename`          VARCHAR(255) NOT NULL,
  `report_type`       VARCHAR(32)  NOT NULL COMMENT 'LAB_REPORT/GENERAL_EXAM_REPORT/MEDICAL_IMAGE',
  `extraction_method` VARCHAR(32)  NOT NULL COMMENT 'IMAGE_OCR/PDF_TEXT/IMAGE_ANALYSIS',
  `risk_level`        VARCHAR(16)  NOT NULL DEFAULT 'LOW',
  `summary`           TEXT         NOT NULL,
  `abnormal_count`    INT          NOT NULL DEFAULT 0,
  `structured_json`   LONGTEXT     NOT NULL COMMENT '指标、知识证据、影像候选等完整结果',
  `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_report_user_time` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体检报告';

SET FOREIGN_KEY_CHECKS = 1;
