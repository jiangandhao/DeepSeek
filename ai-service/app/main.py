"""AI 服务入口 (FastAPI)。

提供:健康检查、通用 DeepSeek 对话。
后续阶段挂载:血糖预测、异常检测、血糖管理智能体、影像推理。
"""
import httpx
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config import settings
from app.routers import agent, analysis, chat, health, imaging, reports
from app.rag.retriever import retriever

app = FastAPI(title=settings.app_name, version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.exception_handler(httpx.HTTPStatusError)
async def deepseek_http_error(request: Request, exc: httpx.HTTPStatusError):
    code = exc.response.status_code
    hint = "请检查 DEEPSEEK_API_KEY 是否正确" if code in (401, 403) else "请稍后重试或检查网络/额度"
    return JSONResponse(status_code=502,
                        content={"detail": f"DeepSeek API 调用失败({code}),{hint}"})


@app.exception_handler(httpx.HTTPError)
async def deepseek_conn_error(request: Request, exc: httpx.HTTPError):
    return JSONResponse(status_code=502,
                        content={"detail": f"无法连接 DeepSeek API:{exc}"})

app.include_router(chat.router)
app.include_router(analysis.router)
app.include_router(agent.router)
app.include_router(health.router)
app.include_router(imaging.router)
app.include_router(reports.router)


@app.get("/health", tags=["meta"])
async def health():
    return {"status": "ok", "service": settings.app_name, "vector_db": retriever.status()}


@app.get("/", tags=["meta"])
async def root():
    return {"service": settings.app_name, "docs": "/docs"}
