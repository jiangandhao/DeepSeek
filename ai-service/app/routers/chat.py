"""通用对话路由:验证 Mimo 连通性,后续供智能体复用。"""
from fastapi import APIRouter
from fastapi.responses import StreamingResponse
import httpx

from app.deepseek.client import deepseek_client
from app.schemas import ChatRequest, ChatResponse

router = APIRouter(prefix="/api/chat", tags=["chat"])


@router.post("", response_model=ChatResponse)
async def chat(req: ChatRequest):
    """非流式对话。"""
    messages = [m.model_dump() for m in req.messages]
    content = await deepseek_client.chat_content(messages, temperature=req.temperature)
    return ChatResponse(content=content)


@router.post("/stream")
async def chat_stream(req: ChatRequest):
    """SSE 流式对话。前端 / 后端按 text/event-stream 消费。"""
    messages = [m.model_dump() for m in req.messages]

    async def event_gen():
        try:
            async for piece in deepseek_client.stream_chat(messages, temperature=req.temperature):
                yield f"data: {piece}\n\n"
        except httpx.HTTPError as e:
            yield f"data: [ERROR] {e}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(event_gen(), media_type="text/event-stream")
