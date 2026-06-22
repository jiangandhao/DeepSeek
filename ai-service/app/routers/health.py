"""AI 数智健管师 + 疾病风险评估路由。"""
from fastapi import APIRouter
from fastapi.responses import StreamingResponse
import httpx

from app.agents.health_manager import generate_health_plan, stream_health_plan
from app.risk.scorer import score_risk
from app.schemas import AdviceRequest, RiskRequest, RiskResponse
from app.plans.generator import generate_structured_plan

router = APIRouter(tags=["health-manager"])


@router.post("/api/risk/assess", response_model=RiskResponse)
async def assess_risk(req: RiskRequest):
    """疾病风险评分(规则模型,无需 LLM,快速)。"""
    return RiskResponse(**score_risk(req.profile, req.glucose))


@router.post("/api/health/plan")
async def health_plan(req: AdviceRequest):
    """AI 数智健管师:风险解读 + 个性化处方(LLM + RAG)。"""
    return await generate_health_plan(req)


@router.post("/api/health/plan/structured")
async def structured_health_plan(req: AdviceRequest):
    """生成结构化饮食、营养目标与七日运动计划，无需调用外部大模型。"""
    return generate_structured_plan(req)


@router.post("/api/health/plan/stream")
async def health_plan_stream(req: AdviceRequest):
    async def event_gen():
        try:
            async for piece in stream_health_plan(req):
                yield f"data: {piece}\n\n"
        except httpx.HTTPError as e:
            yield f"data: [ERROR] {e}\n\n"
        yield "data: [DONE]\n\n"

    return StreamingResponse(event_gen(), media_type="text/event-stream")
