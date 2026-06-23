# AI 服务 (Python / FastAPI)

承载所有 AI 能力:Mimo 调用、RAG 检索、血糖预测、异常检测,后续扩展影像推理。

## 本地开发

```bash
cd ai-service
python3 -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt

# 配置 Mimo
export MIMO_API_KEY=sk-xxx
# 或在 ai-service/ 下创建 .env 文件

uvicorn app.main:app --reload --port 8000
```

打开 http://localhost:8000/docs 查看交互式 API 文档。

## 已实现接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/health` | 健康检查 |
| POST | `/api/chat` | Mimo 非流式对话 |
| POST | `/api/chat/stream` | Mimo SSE 流式对话 |

## 验证 Mimo 连通

```bash
curl -X POST http://localhost:8000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"messages":[{"role":"user","content":"你好"}]}'
```

## 目录(规划)

```
app/
├── deepseek/     # Mimo 客户端封装 ✅
├── routers/      # 路由 ✅(chat),后续 prediction/anomaly/agent
├── rag/          # 知识库加载/检索(阶段3)
├── prediction/   # 血糖预测模型(阶段2)
├── anomaly/      # 异常检测(阶段2)
└── agents/       # 血糖管理智能体编排(阶段3)
```
