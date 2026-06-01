# DeepSeek 全流程健康管理系统

基于 DeepSeek 大模型的全流程健康管理系统，融合 AI 技术与医疗健康数据，实现个性化健康管理。

## 项目简介

本系统是一个融合 DeepSeek 大模型与医疗健康数据的全流程健康管理体系，涵盖血糖管理、AI 数智健管师、智能体检优化、疾病风险预警等核心功能。

### 核心功能

- **血糖管理 AI 智能体**：分析血糖/饮食/运动数据，生成个性化饮食调整与运动方案
- **AI 数智健管师**：综合家族病史/体检数据评估疾病风险，提供个性化健康建议
- **智能体检优化**：智能预约、影像识别、报告可视化
- **疾病风险预警**：基于长期健康数据的风险分级与预警

## 技术架构

### 四层架构

```
┌─────────────────────────────────────────┐
│           支撑层 (Support Layer)          │
│    DeepSeek-R1 高性能 AI 计算平台         │
├─────────────────────────────────────────┤
│           应用层 (Application Layer)      │
│    血糖管理 | 数智健管师 | 体检优化        │
├─────────────────────────────────────────┤
│           模型层 (Model Layer)            │
│    血糖模型 | 风险评估 | 影像识别          │
├─────────────────────────────────────────┤
│           数据层 (Data Layer)             │
│    体检数据 | 健康档案 | 疾病管理          │
└─────────────────────────────────────────┘
```

### 技术栈

| 层级 | 技术选型 |
|------|---------|
| **前端** | Vue.js 3 + Element Plus + ECharts |
| **后端** | Java + Spring Boot + MyBatis-Plus |
| **AI 模型** | Python + PyTorch + TensorFlow |
| **数据库** | MySQL 8.0 + Redis |
| **部署** | Docker + Nginx |

## 项目结构

```
deepseek-health-management/
├── frontend/                    # 前端项目 (Vue.js)
├── backend/                     # 后端项目 (Java)
├── ai-model/                    # AI模型项目 (Python)
├── data/                        # 数据资源
├── docs/                        # 项目文档
└── deploy/                      # 部署配置
```

## 快速开始

### 环境要求

- Node.js >= 16
- Java >= 17
- Python >= 3.8
- MySQL >= 8.0
- Docker (可选)

### 本地开发

#### 1. 启动数据库

```bash
# 使用 Docker 启动 MySQL
docker run -d --name health-mysql \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=health_management \
  -p 3306:3306 \
  mysql:8.0

# 初始化数据库
mysql -u root -p health_management < data/database/init.sql
```

#### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

#### 3. 启动 AI 模型服务

```bash
cd ai-model
pip install -r requirements.txt
python inference/api.py
```

#### 4. 启动前端

```bash
cd frontend
npm install
npm run serve
```

### Docker 部署

```bash
cd deploy
docker-compose up -d
```

## API 接口

### 血糖管理

- `GET /api/blood-sugar/records` - 获取血糖记录
- `POST /api/blood-sugar/records` - 添加血糖记录
- `GET /api/blood-sugar/trend` - 获取血糖趋势
- `POST /api/blood-sugar/diet/recommendation` - 获取饮食建议

### 健康管理

- `POST /api/health-manager/risk-assessment` - 健康风险评估
- `POST /api/health-manager/diet-prescription` - 获取饮食处方
- `POST /api/health-manager/exercise-plan` - 获取运动计划

### AI 模型

- `POST /api/ai/blood-sugar/predict` - 血糖预测
- `POST /api/ai/risk-assessment/predict` - 风险评估预测
- `POST /api/ai/deepseek/recommendation` - DeepSeek 健康建议

## 配置说明

### DeepSeek API 配置

在 `ai-model/config.yaml` 中配置：

```yaml
deepseek:
  api_key: "your-api-key-here"
  api_url: "https://api.deepseek.com"
  model: "deepseek-chat"
```

或设置环境变量：

```bash
export DEEPSEEK_API_KEY=your-api-key-here
```

### 数据库配置

在 `backend/src/main/resources/application.yml` 中配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/health_management
    username: root
    password: root123
```

## 功能模块

### 1. 血糖管理 AI 智能体

- 实时血糖数据记录与分析
- 血糖趋势预测
- 个性化饮食建议
- 运动方案推荐
- 异常波动预警

### 2. AI 数智健管师

- 疾病风险评估
- 个性化饮食处方
- 运动计划调整
- 疾病预防建议

### 3. 智能体检优化

- 智能预约推荐
- 体检报告可视化
- 影像识别分析
- 指标趋势展示

### 4. 疾病风险预警

- 风险分级管理
- 早期预警通知
- 预防方案推荐

## 开发指南

### 代码规范

- 前端：ESLint + Prettier
- 后端：阿里巴巴 Java 开发手册
- Python：PEP 8

### 提交规范

- feat: 新功能
- fix: 修复 bug
- docs: 文档更新
- style: 代码格式调整
- refactor: 重构
- test: 测试相关
- chore: 构建/工具相关

## 许可证

本项目仅用于学习和实训目的。

## 联系方式

如有问题，请联系项目负责人。
