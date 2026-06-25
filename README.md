# 基于 Mimo 的全流程健康管理系统

融合 Mimo 大模型与医疗健康数据的智能健康管理系统(毕业设计)。首期聚焦**血糖管理智能体**,打通「Vue 前端 → Java 后端 → Python AI 服务 → Mimo」全链路。

## 架构

```
Vue3 前端  ──REST/SSE──▶  Spring Boot 后端  ──REST──▶  Python FastAPI AI 服务  ──▶  Mimo API
                              │                              │
                         MySQL + Redis                  向量库(Chroma)+ 医疗知识库
```

## 目录

| 目录 | 说明 |
|---|---|
| `frontend/` | Vue 3 + Vite + Element Plus + ECharts 前端 |
| `backend/` | Spring Boot 3 业务后端(认证、数据 CRUD、AI 编排网关) |
| `ai-service/` | Python FastAPI AI 服务(Mimo、RAG、血糖预测、异常检测) |
| `deploy/` | docker-compose、数据库初始化 SQL |
| `docs/` | 毕设文档:需求/架构/接口/论文素材 |

## 技术栈

- **前端**:Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios、ECharts
- **后端**:Java 17、Spring Boot 3、Spring Security(JWT)、MyBatis-Plus、Redis
- **AI 服务**:Python 3.12、FastAPI、httpx、Tesseract OCR、持久化 SQLite 向量库
- **数据库**:MySQL 8、Redis、Chroma(向量库)
- **大模型**:Mimo API（OpenAI 兼容格式，默认模型 mimo）

## 快速开始

### 1. 配置环境变量

复制 `.env.example` 为 `.env`,填入 Mimo API Key:

```bash
cp .env.example .env
# 编辑 .env,设置 MIMO_API_KEY=sk-xxx
```

### 2. 一键启动(Docker)

```bash
cd deploy
docker compose --env-file ../.env up -d --build
```

- 前端:http://localhost:5173
- 后端:http://localhost:8080
- AI 服务:http://localhost:8000/docs
- MySQL:localhost:3306,Redis:localhost:6379

### 3. 导入演示数据(答辩用)

```bash
python deploy/seed_demo.py            # 默认连 http://localhost:8080
# 之后用 demo / demo123 登录前端
```

### 4. 本地开发

各子项目的本地启动方式见各自目录下的 README。

### 5. 论文素材脚本

```bash
cd ai-service
python notebooks/glucose_forecast_experiment.py   # 预测模型对比(MAE/RMSE)
python notebooks/federated_learning_demo.py        # 联邦学习 FedAvg 对比实验
python data/generate_synthetic.py --days 60        # 合成血糖数据

python deploy/loadtest.py --requests 600 --concurrency 50   # 并发压测(QPS/分位延迟)
```

## 文档

- [交付清单与验收](docs/DELIVERABLES.md)
- [系统架构](docs/architecture.md)
- [接口文档](docs/api.md)
- [数据库设计](docs/database.md)
- [高并发架构与压测](docs/high-concurrency.md)
- [论文大纲与技术亮点](docs/thesis-outline.md)

## 测试

```bash
cd ai-service && python -m pytest          # AI 服务单测(20 passed)
cd backend && mvn test                     # 后端单测(7 passed,AesUtil/ExamService)
```

## 实施路线图

- [x] 阶段 0:三端脚手架 + Docker 编排(5 服务一键启动已验证)
- [x] 阶段 1:用户认证 + 血糖/饮食/运动数据闭环 + ECharts 趋势图
- [x] 阶段 2:血糖预测模型 + 异常预警(含对比实验)
- [x] 阶段 3:RAG 知识库 + Mimo 血糖智能体 + SSE 流式对话
- [x] 阶段 4:AI 数智健管师 / 疾病风险预警 / 智能体检预约 / 影像识别(肺结节演示)
- [ ] 阶段 5:联邦学习/脱敏/高并发架构 + 完整论文素材

> 阶段 0-4 已端到端跑通。AI 对话/方案需在 `.env` 配置真实 `MIMO_API_KEY` 后生效(数据/预测/预警/体检/影像功能无需 key)。

## 功能模块一览

| 菜单 | 功能 |
|---|---|
| 血糖概览 | 趋势图(正常区间/异常点标注)+ 统计卡片 |
| AI 智能体 | 血糖管理智能体(RAG+Mimo)流式对话 + 预测/预警面板 |
| 数智健管师 | 健康档案 + 疾病风险评分(可解释)+ AI 个性化处方 + 预警记录 |
| 个人身份卡 | 身份信息、健康信息、BMI、档案完整度与隐私说明 |
| 智能设备 | 虚拟设备绑定、状态查看、接口数据模拟上报 |
| 管理员运维 | 用户管理、近 7 日日活、设备在线与电量管理 |
| 智能体检 | 套餐与机构推荐、预约管理、报告历史归档 |
| 报告分析 | 体检单/化验单图片与 PDF OCR、指标异常识别、向量知识检索、医学图片辅助分析 |
| 个性方案 | 个体营养目标、4 餐食谱与替换项、七日运动处方、监测建议 |
| 血糖/饮食/运动 | 数据录入与管理 |
