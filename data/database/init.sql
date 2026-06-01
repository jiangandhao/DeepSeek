-- DeepSeek 健康管理系统 - 数据库初始化脚本

USE health_management;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    real_name VARCHAR(50),
    age INT,
    gender TINYINT DEFAULT 0 COMMENT '0:未知, 1:男, 2:女',
    avatar VARCHAR(255),
    status TINYINT DEFAULT 1 COMMENT '0:禁用, 1:启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 健康档案表
CREATE TABLE IF NOT EXISTS health_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    blood_type VARCHAR(5) COMMENT '血型',
    allergy_history TEXT COMMENT '过敏史',
    medical_history TEXT COMMENT '病史',
    family_disease_history TEXT COMMENT '家族病史',
    birthday DATE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 血糖记录表
CREATE TABLE IF NOT EXISTS blood_sugar_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    value DECIMAL(4,1) NOT NULL COMMENT '血糖值(mmol/L)',
    type VARCHAR(20) NOT NULL COMMENT 'fasting:空腹, before:餐前, after:餐后',
    measure_time DATETIME NOT NULL COMMENT '测量时间',
    note TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_measure_time (measure_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 体检报告表
CREATE TABLE IF NOT EXISTS checkup_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_no VARCHAR(50) NOT NULL UNIQUE COMMENT '报告编号',
    checkup_date DATE NOT NULL COMMENT '体检日期',
    hospital_name VARCHAR(100) COMMENT '医院名称',
    report_url VARCHAR(255) COMMENT '报告文件URL',
    summary TEXT COMMENT '报告摘要',
    risk_level TINYINT DEFAULT 0 COMMENT '0:正常, 1:低风险, 2:中风险, 3:高风险',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_report_no (report_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 健康风险评估表
CREATE TABLE IF NOT EXISTS risk_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    risk_type VARCHAR(50) NOT NULL COMMENT '风险类型',
    risk_level TINYINT NOT NULL COMMENT '1:低风险, 2:中风险, 3:高风险',
    risk_score DECIMAL(5,2) COMMENT '风险评分',
    description TEXT COMMENT '风险描述',
    suggestions TEXT COMMENT '建议措施',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_risk_type (risk_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 饮食记录表
CREATE TABLE IF NOT EXISTS diet_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    meal_type VARCHAR(20) NOT NULL COMMENT 'breakfast:早餐, lunch:午餐, dinner:晚餐, snack:加餐',
    food_name VARCHAR(100) NOT NULL COMMENT '食物名称',
    food_amount DECIMAL(8,2) COMMENT '食物用量(g)',
    calorie DECIMAL(8,2) COMMENT '热量(kcal)',
    carbohydrate DECIMAL(8,2) COMMENT '碳水化合物(g)',
    protein DECIMAL(8,2) COMMENT '蛋白质(g)',
    fat DECIMAL(8,2) COMMENT '脂肪(g)',
    record_date DATE NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 运动记录表
CREATE TABLE IF NOT EXISTS exercise_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    exercise_type VARCHAR(50) NOT NULL COMMENT '运动类型',
    duration INT COMMENT '运动时长(分钟)',
    calorie_consumed DECIMAL(8,2) COMMENT '消耗热量(kcal)',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    note TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 体检预约表
CREATE TABLE IF NOT EXISTS checkup_appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hospital_name VARCHAR(100) NOT NULL COMMENT '医院名称',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    appointment_time VARCHAR(20) COMMENT '预约时段',
    checkup_items TEXT COMMENT '体检项目',
    status TINYINT DEFAULT 0 COMMENT '0:待确认, 1:已确认, 2:已完成, 3:已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_appointment_date (appointment_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试数据
INSERT INTO users (username, password, phone, email, real_name, age, gender) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 'test@example.com', '测试用户', 35, 1);

INSERT INTO health_profiles (user_id, height, weight, blood_type, birthday) VALUES
(1, 175.00, 70.00, 'A', '1990-01-01');

INSERT INTO blood_sugar_records (user_id, value, type, measure_time) VALUES
(1, 5.8, 'fasting', '2024-01-15 07:00:00'),
(1, 7.2, 'after', '2024-01-15 09:00:00'),
(1, 5.6, 'fasting', '2024-01-16 07:00:00'),
(1, 7.8, 'after', '2024-01-16 09:00:00'),
(1, 6.0, 'fasting', '2024-01-17 07:00:00'),
(1, 7.5, 'after', '2024-01-17 09:00:00');

-- 血压记录表
CREATE TABLE IF NOT EXISTS blood_pressure_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    systolic INT NOT NULL COMMENT '收缩压(mmHg)',
    diastolic INT NOT NULL COMMENT '舒张压(mmHg)',
    heart_rate INT COMMENT '心率(bpm)',
    measure_time DATETIME NOT NULL COMMENT '测量时间',
    note TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_measure_time (measure_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 慢性病管理表
CREATE TABLE IF NOT EXISTS chronic_disease_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    disease_type VARCHAR(50) NOT NULL COMMENT '疾病类型: diabetes/hypertension/heart_disease',
    diagnosed_date DATE COMMENT '确诊时间',
    medication VARCHAR(255) COMMENT '当前用药',
    control_target VARCHAR(255) COMMENT '控制目标',
    last_checkup_date DATE COMMENT '最近复查日期',
    status TINYINT DEFAULT 1 COMMENT '0:已康复, 1:控制中, 2:需关注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_disease_type (disease_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用药记录表
CREATE TABLE IF NOT EXISTS medication_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    medication_name VARCHAR(100) NOT NULL COMMENT '药物名称',
    dosage VARCHAR(50) COMMENT '剂量',
    frequency VARCHAR(50) COMMENT '服用频率',
    purpose VARCHAR(100) COMMENT '用途',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    status TINYINT DEFAULT 1 COMMENT '0:已停用, 1:使用中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预警记录表
CREATE TABLE IF NOT EXISTS warning_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    warning_type VARCHAR(50) NOT NULL COMMENT '预警类型',
    warning_level VARCHAR(20) NOT NULL COMMENT '预警级别: high/medium/low',
    title VARCHAR(200) NOT NULL COMMENT '预警标题',
    content TEXT COMMENT '预警内容',
    action_suggestion TEXT COMMENT '处理建议',
    is_read TINYINT DEFAULT 0 COMMENT '0:未读, 1:已读',
    is_dismissed TINYINT DEFAULT 0 COMMENT '0:未忽略, 1:已忽略',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_time DATETIME COMMENT '阅读时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_warning_level (warning_level),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 预警阈值设置表
CREATE TABLE IF NOT EXISTS warning_thresholds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    threshold_type VARCHAR(50) NOT NULL COMMENT '阈值类型',
    threshold_value DECIMAL(10,2) NOT NULL COMMENT '阈值',
    unit VARCHAR(20) COMMENT '单位',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_type (user_id, threshold_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 健康目标表
CREATE TABLE IF NOT EXISTS health_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_name VARCHAR(100) NOT NULL COMMENT '目标名称',
    target_value VARCHAR(100) COMMENT '目标值',
    current_value VARCHAR(100) COMMENT '当前值',
    progress INT DEFAULT 0 COMMENT '进度(0-100)',
    status VARCHAR(20) DEFAULT 'in-progress' COMMENT '状态: in-progress/completed/failed',
    start_date DATE COMMENT '开始日期',
    target_date DATE COMMENT '目标日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 影像分析记录表
CREATE TABLE IF NOT EXISTS image_analysis_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    task_id VARCHAR(50) NOT NULL UNIQUE COMMENT '任务ID',
    image_type VARCHAR(20) NOT NULL COMMENT '影像类型: ct/xray/ultrasound',
    image_url VARCHAR(255) COMMENT '影像URL',
    analysis_result TEXT COMMENT '分析结果(JSON)',
    diagnosis TEXT COMMENT '诊断意见',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态: pending/processing/completed/failed',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    complete_time DATETIME COMMENT '完成时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入更多测试数据
INSERT INTO blood_pressure_records (user_id, systolic, diastolic, heart_rate, measure_time) VALUES
(1, 128, 82, 72, '2024-01-15 08:00:00'),
(1, 135, 88, 75, '2024-01-16 08:00:00'),
(1, 125, 80, 70, '2024-01-17 08:00:00');

INSERT INTO chronic_disease_records (user_id, disease_type, diagnosed_date, medication, control_target) VALUES
(1, 'diabetes', '2023-06-15', 'metformin 500mg', 'fasting<7.0, postprandial<10.0'),
(1, 'hypertension', '2022-03-10', 'amlodipine 5mg', '<140/90 mmHg');

INSERT INTO medication_records (user_id, medication_name, dosage, frequency, purpose, status) VALUES
(1, '二甲双胍', '500mg', '每日2次', '控制血糖', 1),
(1, '氨氯地平', '5mg', '每日1次', '控制血压', 1),
(1, '阿托伐他汀', '20mg', '每晚1次', '调节血脂', 1);

INSERT INTO warning_records (user_id, warning_type, warning_level, title, content, is_read) VALUES
(1, 'blood_sugar', 'high', '血糖严重超标预警', '您今日空腹血糖测量值为8.2mmol/L，已超过警戒线。', 0),
(1, 'blood_pressure', 'medium', '血压波动预警', '近3天血压测量值波动较大', 0),
(1, 'weight', 'low', '体重增加提醒', '近一个月体重增加2kg', 1);

INSERT INTO warning_thresholds (user_id, threshold_type, threshold_value, unit) VALUES
(1, 'blood_sugar_high', 7.0, 'mmol/L'),
(1, 'blood_pressure_high', 140, 'mmHg'),
(1, 'heart_rate_high', 100, 'bpm'),
(1, 'bmi_high', 24, '');

INSERT INTO health_goals (user_id, goal_name, target_value, current_value, progress, status) VALUES
(1, '血糖达标', '空腹<6.1mmol/L', '6.8mmol/L', 60, 'in-progress'),
(1, '体重控制', 'BMI<24', 'BMI 25.4', 40, 'in-progress'),
(1, '血压稳定', '<130/85mmHg', '128/82mmHg', 80, 'in-progress');
