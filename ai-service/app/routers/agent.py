"""血糖管理智能体路由。"""
from fastapi import APIRouter
from fastapi.responses import StreamingResponse
import httpx

import base64

from app.agents.glucose_agent import generate_advice, stream_advice
from app.agents.insight_agent import generate_insight, stream_insight
from app.rag.retriever import retriever
from app.schemas import AdviceRequest, AdviceResponse, InsightRequest

router = APIRouter(prefix="/api/agent", tags=["agent"])


@router.post("/advice", response_model=AdviceResponse)
async def advice(req: AdviceRequest):
    """生成血糖管理方案 / 回答用户健康问题(非流式)。"""
    result = await generate_advice(req)
    return AdviceResponse(**result)


@router.post("/advice/stream")
async def advice_stream(req: AdviceRequest):
    """SSE 流式生成。"""
    async def event_gen():
        try:
            async for piece in stream_advice(req):
                yield f"data: {piece}\n\n"
        except httpx.HTTPError as e:
            yield f"data: [ERROR] {e}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(event_gen(), media_type="text/event-stream")


@router.post("/insight")
async def insight(req: InsightRequest):
    """结构化洞察:返回可直接渲染为卡片的 JSON(总览/指标/分节/建议)。"""
    return await generate_insight(req)


@router.post("/insight/stream")
async def insight_stream(req: InsightRequest):
    """流式洞察:逐字输出 Markdown。为穿越双层 SSE 不丢换行,正文按 base64 编码。"""
    async def event_gen():
        try:
            async for piece in stream_insight(req):
                if not piece:
                    continue
                yield "data: " + base64.b64encode(piece.encode("utf-8")).decode("ascii") + "\n\n"
        except httpx.HTTPError as e:
            yield f"data: [ERROR] {e}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(event_gen(), media_type="text/event-stream")


@router.get("/retrieve")
async def retrieve(q: str, k: int = 3):
    """检索调试接口(供论文展示 RAG 召回)。"""
    return {"query": q, "results": retriever.retrieve(q, k)}
