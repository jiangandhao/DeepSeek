# 基于 DeepSeek/Mimo 的全流程健康管理系统

本项目是一个面向健康数据闭环管理的全栈系统，覆盖用户注册登录、健康档案、血糖监测、饮食运动记录、AI 智能问答、个性化健康方案、体检预约、报告分析、智能设备模拟接入和管理员运维等流程。

系统由 Vue 3 前端、Spring Boot 后端、FastAPI AI 服务、MySQL 和 Redis 组成。最新版本已在仓库根目录提供 `Makefile`，推荐使用 `make` 完成一键部署、服务管理和日志查看。

## 项目架构

```text
Vue 3 前端
    |
    | REST / SSE
    v
Spring Boot 后端  ---- MySQL
    |               ---- Redis
    |
    | REST
    v
FastAPI AI 服务 ---- DeepSeek/Mimo 兼容大模型接口
    |
    +---- RAG 知识库、预测、预警、报告分析、影像识别
```

## 目录说明

| 目录 | 说明 |
| --- | --- |
| `frontend/` | Vue 3 + Vite + Element Plus 前端项目 |
| `backend/` | Spring Boot 3 后端服务，提供认证、业务接口和 AI 编排 |
| `ai-service/` | FastAPI AI 服务，包含智能体、RAG、预测、报告分析和影像识别 |
| `deploy/` | Docker Compose、数据库初始化 SQL、演示数据和压测脚本 |
| `docs/` | 架构、接口、数据库、交付物和论文相关文档 |
| `Makefile` | 推荐入口，封装 Docker Compose 常用命令 |

## 技术栈

| 层级 | 技术 |
| --- | --- |
| 前端 | Vue 3、Vite、Element Plus、Vue Router、Pinia、Axios、ECharts |
| 后端 | Java 17、Spring Boot 3、Spring Security、JWT、MyBatis-Plus |
| AI 服务 | Python、FastAPI、httpx、RAG、预测模型、OCR/报告分析 |
| 数据层 | MySQL 8、Redis、Chroma 向量数据 |
| 部署 | Docker、Docker Compose、Makefile |

## 快速开始

### 1. 准备环境

请先安装：

- Docker Desktop 或 Docker Engine
- Docker Compose v2
- `make`

Windows 如果没有 `make`，可以使用 Git Bash、MSYS2、WSL，或直接执行本文后面的 Docker Compose 原始命令。

### 2. 配置环境变量

在仓库根目录复制环境变量文件：

```bash
cp .env.example .env
```

然后编辑 `.env`，至少确认以下配置：

```env
MIMO_API_KEY=sk-replace-me
MIMO_BASE_URL=https://api.xiaomimimo.com/v1
MIMO_MODEL=mimo-v2.5

MYSQL_ROOT_PASSWORD=root123456
MYSQL_DATABASE=health
MYSQL_USER=health
MYSQL_PASSWORD=health123456

JWT_SECRET=change-this-to-a-long-random-secret-key-at-least-32-bytes
JWT_EXPIRE_MINUTES=1440
AI_SERVICE_URL=http://ai-service:8000
```

如果需要接入 DeepSeek 或其他 OpenAI 兼容服务，可将 `MIMO_BASE_URL`、`MIMO_MODEL` 和 `MIMO_API_KEY` 替换为对应服务的地址、模型名和密钥。

### 3. 一键部署

在仓库根目录执行：

```bash
make deploy
```

该命令会构建并启动全部服务：

- MySQL
- Redis
- AI 服务
- 后端服务
- 前端服务

启动完成后访问：

| 服务 | 地址 |
| --- | --- |
| 前端系统 | http://localhost:5173 |
| 后端接口 | http://localhost:8080 |
| AI 服务文档 | http://localhost:8000/docs |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |

## Makefile 使用说明

`Makefile` 已封装常用部署命令，均在仓库根目录执行。

| 命令 | 作用 |
| --- | --- |
| `make deploy` | 构建镜像并启动全部服务 |
| `make up` | 启动已有镜像和容器 |
| `make down` | 停止并移除容器 |
| `make ps` | 查看服务运行状态 |
| `make logs` | 查看全部服务日志 |
| `make logs SERVICE=backend` | 查看后端日志 |
| `make logs SERVICE=frontend` | 查看前端日志 |
| `make logs SERVICE=ai-service` | 查看 AI 服务日志 |
| `make backend` | 重新构建并启动后端 |
| `make frontend` | 重新构建并启动前端 |
| `make ai-service` | 重新构建并启动 AI 服务 |
| `make mysql` | 单独启动 MySQL |
| `make redis` | 单独启动 Redis |

等价的 Docker Compose 原始命令如下：

```bash
docker compose --env-file .env -p deploy -f deploy/docker-compose.yml up -d --build
docker compose --env-file .env -p deploy -f deploy/docker-compose.yml ps
docker compose --env-file .env -p deploy -f deploy/docker-compose.yml logs -f --tail=200 backend
docker compose --env-file .env -p deploy -f deploy/docker-compose.yml down
```

## 登录账号

系统首次启动后会自动初始化管理员账号：

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | `admin` | `admin123` | 可访问管理员运维页面 |

普通用户可以在登录页自行注册。系统禁止用户注册名为 `admin` 的账号，管理员账号由后端初始化。

如需导入演示数据，可在服务启动后执行：

```bash
python deploy/seed_demo.py
```

脚本默认连接 `http://localhost:8080`，导入后可按脚本输出的账号信息登录。

## 功能模块

| 模块 | 说明 |
| --- | --- |
| 用户认证 | 注册、登录、JWT 鉴权、路由守卫 |
| 首页仪表盘 | 健康指标概览、趋势和统计信息 |
| 血糖管理 | 血糖记录、趋势分析、异常标记、预测预警 |
| 饮食记录 | 膳食记录、营养目标和饮食建议 |
| 运动记录 | 运动数据录入、消耗统计和运动建议 |
| AI 智能体 | 基于 RAG 和大模型的流式健康问答 |
| 数智健管师 | 健康档案、风险评分、个性化处方和预警 |
| 个人身份卡 | 身份信息、健康信息、BMI 和档案完整度 |
| 智能设备 | 虚拟设备绑定、设备状态、模拟数据上报 |
| 管理员运维 | 用户管理、近 7 日日活、设备在线和电量管理 |
| 智能体检 | 套餐推荐、机构推荐、预约管理和报告归档 |
| 报告分析 | 体检单/化验单 OCR、异常指标识别和知识检索 |
| 影像识别 | 医学影像演示分析 |
| 个性方案 | 营养目标、食谱、运动处方和监测建议 |

## 本地开发

推荐优先使用 Docker + Makefile 运行完整环境。需要单独调试某个服务时，可按下面方式启动。

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务默认访问后端 `http://localhost:8080`。

构建验证：

```bash
cd frontend
npm run build
```

### 后端

```bash
cd backend
mvn spring-boot:run
```

本地运行后端前，请确保 MySQL 和 Redis 已启动，并在环境变量或 `application.yml` 中配置数据库、Redis、JWT 和 AI 服务地址。

编译或测试：

```bash
cd backend
mvn test
```

### AI 服务

```bash
cd ai-service
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

Windows PowerShell 激活虚拟环境：

```powershell
cd ai-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

AI 服务测试：

```bash
cd ai-service
python -m pytest
```

## 数据库说明

数据库初始化脚本位于：

```text
deploy/init.sql
```

Docker 首次创建 MySQL 数据卷时会自动执行该脚本。若已经启动过 MySQL，修改 `init.sql` 后不会自动重新导入，需要手动执行 SQL 或清理 Docker volume 后重新启动。

谨慎清理数据卷：

```bash
make down
docker volume rm deploy_mysql_data deploy_redis_data deploy_chroma_data
make deploy
```

## 常用运维命令

查看容器状态：

```bash
make ps
```

查看全部日志：

```bash
make logs
```

查看指定服务日志：

```bash
make logs SERVICE=backend
make logs SERVICE=ai-service
make logs SERVICE=frontend
```

重启单个服务：

```bash
make backend
make ai-service
make frontend
```

停止全部服务：

```bash
make down
```

## 文档索引

- [交付物清单](docs/DELIVERABLES.md)
- [系统架构](docs/architecture.md)
- [接口文档](docs/api.md)
- [数据库设计](docs/database.md)
- [高并发架构与压测](docs/high-concurrency.md)
- [论文大纲与技术亮点](docs/thesis-outline.md)

## 实验与脚本

生成合成血糖数据：

```bash
python ai-service/data/generate_synthetic.py --days 60
```

运行预测模型对比实验：

```bash
cd ai-service
python notebooks/glucose_forecast_experiment.py
```

运行联邦学习演示：

```bash
cd ai-service
python notebooks/federated_learning_demo.py
```

运行接口压测：

```bash
python deploy/loadtest.py --requests 600 --concurrency 50
```

## 常见问题

### 1. 执行 `make deploy` 提示没有 make

Windows 可以安装 Git Bash、MSYS2、Chocolatey make 或使用 WSL。也可以直接使用 Docker Compose 原始命令：

```bash
docker compose --env-file .env -p deploy -f deploy/docker-compose.yml up -d --build
```

### 2. AI 问答没有返回大模型内容

请检查 `.env` 中的 `MIMO_API_KEY`、`MIMO_BASE_URL` 和 `MIMO_MODEL` 是否正确，然后重启 AI 服务：

```bash
make ai-service
```

### 3. 后端无法连接数据库

先查看 MySQL 和后端状态：

```bash
make ps
make logs SERVICE=mysql
make logs SERVICE=backend
```

确认 `.env` 中的 MySQL 用户名、密码和数据库名与容器配置一致。

### 4. 修改前端后页面没有变化

Docker 部署模式下需要重新构建前端镜像：

```bash
make frontend
```

本地开发模式下使用：

```bash
cd frontend
npm run dev
```

### 5. 需要重置数据库

如果只是清空业务数据，建议通过数据库客户端手动处理。若需要完整重建 Docker 数据卷，可执行：

```bash
make down
docker volume rm deploy_mysql_data
make deploy
```

该操作会删除 MySQL 容器数据，请提前备份。

## 提交说明

推荐提交前至少执行：

```bash
cd frontend
npm run build
```

如果本地安装了 Maven，也建议执行：

```bash
cd backend
mvn test
```

完整部署验证可执行：

```bash
make deploy
make ps
```
