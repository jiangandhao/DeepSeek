USE health_management;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    real_name VARCHAR(50),
    age INT,
    gender INT,
    avatar VARCHAR(255),
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS health_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    height DOUBLE,
    weight DOUBLE,
    blood_type VARCHAR(10),
    allergy_history TEXT,
    medical_history TEXT,
    family_disease_history TEXT,
    birthday DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS blood_sugar_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    value DOUBLE NOT NULL,
    type VARCHAR(20),
    measure_time DATETIME,
    note VARCHAR(500),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS checkup_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    report_no VARCHAR(50),
    checkup_date DATETIME,
    hospital_name VARCHAR(100),
    report_url VARCHAR(500),
    summary TEXT,
    risk_level INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS risk_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    risk_type VARCHAR(50),
    risk_level INT,
    risk_score DOUBLE,
    description TEXT,
    suggestions TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS warning_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    warning_type VARCHAR(50),
    warning_level VARCHAR(20),
    title VARCHAR(200),
    content TEXT,
    is_read INT DEFAULT 0,
    is_dismissed INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS warning_thresholds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    threshold_type VARCHAR(50),
    threshold_value DOUBLE,
    unit VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS health_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_name VARCHAR(100),
    target_value DOUBLE,
    current_value DOUBLE,
    progress INT DEFAULT 0,
    status INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS checkup_appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    hospital_name VARCHAR(100),
    appointment_date DATETIME,
    appointment_time VARCHAR(20),
    checkup_items TEXT,
    status INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS image_analysis_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    task_id VARCHAR(100),
    image_type VARCHAR(50),
    image_url VARCHAR(500),
    analysis_result TEXT,
    diagnosis TEXT,
    status VARCHAR(20) DEFAULT 'pending',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert test user
INSERT INTO users (username, password, real_name, age, gender, phone, email) VALUES
('admin', 'admin123', 'zhangsan', 35, 1, '13800138000', 'admin@health.com')
ON DUPLICATE KEY UPDATE username=username;

-- Insert health profile
INSERT INTO health_profiles (user_id, height, weight, blood_type, allergy_history, medical_history, birthday) VALUES
(1, 175.0, 70.0, 'A', 'none', 'diabetes type 2', '1990-01-01')
ON DUPLICATE KEY UPDATE user_id=user_id;

-- Insert blood sugar records
INSERT INTO blood_sugar_records (user_id, value, type, measure_time, note) VALUES
(1, 5.8, 'fasting', '2024-01-10 08:00:00', 'normal'),
(1, 6.1, 'fasting', '2024-01-11 08:00:00', 'slightly high'),
(1, 5.9, 'fasting', '2024-01-12 08:00:00', 'normal'),
(1, 6.2, 'fasting', '2024-01-13 08:00:00', 'slightly high'),
(1, 5.7, 'fasting', '2024-01-14 08:00:00', 'normal'),
(1, 6.0, 'fasting', '2024-01-15 08:00:00', 'normal'),
(1, 7.2, 'after', '2024-01-10 10:00:00', 'postprandial'),
(1, 7.8, 'after', '2024-01-11 10:00:00', 'postprandial'),
(1, 7.5, 'after', '2024-01-12 10:00:00', 'postprandial'),
(1, 8.1, 'after', '2024-01-13 10:00:00', 'postprandial high'),
(1, 7.3, 'after', '2024-01-14 10:00:00', 'postprandial'),
(1, 7.6, 'after', '2024-01-15 10:00:00', 'postprandial');

-- Insert warning records
INSERT INTO warning_records (user_id, warning_type, warning_level, title, content, is_read) VALUES
(1, 'blood_sugar', 'high', 'blood sugar warning', 'fasting blood sugar 8.2mmol/L exceeds threshold', 0),
(1, 'blood_pressure', 'medium', 'blood pressure warning', 'blood pressure fluctuation detected', 0),
(1, 'weight', 'low', 'weight reminder', 'weight increased 2kg in last month', 1);

-- Insert warning thresholds
INSERT INTO warning_thresholds (user_id, threshold_type, threshold_value, unit) VALUES
(1, 'blood_sugar_fasting', 7.0, 'mmol/L'),
(1, 'blood_sugar_postprandial', 10.0, 'mmol/L'),
(1, 'blood_pressure_systolic', 140, 'mmHg'),
(1, 'bmi', 24, 'kg/m2');

-- Insert health goals
INSERT INTO health_goals (user_id, goal_name, target_value, current_value, progress, status) VALUES
(1, 'fasting blood sugar', 6.0, 5.9, 90, 1),
(1, 'weight control', 65.0, 70.0, 30, 0),
(1, 'blood pressure', 130.0, 135.0, 60, 0);
