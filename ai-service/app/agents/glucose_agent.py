"""血糖管理智能体编排。

流程:汇总用户数据 -> RAG 检索相关医学知识 -> 组装结构化 Prompt -> 调用 Mimo。
支持非流式与 SSE 流式输出。
"""
from typing import AsyncIterator

from app.anomaly.detector import detect
from app.deepseek.client import deepseek_client
from app.rag.retriever import retriever
from app.schemas import AdviceRequest

SYSTEM_PROMPT = (
    "你是一名专业的糖尿病健康管理助手(AI 数智健管师)。"
    "请基于用户的真实血糖/饮食/运动数据,并参考提供的【医学知识】,"
    "给出科学、个性化、可执行的建议。要求:\n"
    "1. 语言通俗、条理清晰,使用 Markdown 分点表达;\n"
    "2. 结合数据中的异常点(如高血糖/低血糖/波动)进行针对性说明;\n"
    "3. 给出【饮食调整】与【运动方案】两部分具体建议;\n"
    "4. 不开具处方药剂量,涉及用药调整时提示遵医嘱;\n"
    "5. 适当给出温和的健康提醒,避免制造焦虑。"
)


def _summarize(req: AdviceRequest) -> str:
    lines: list[str] = []
    if req.profile:
        p = req.profile
        prof = "、".join(f"{k}:{v}" for k, v in p.items() if v not in (None, ""))
        if prof:
            lines.append(f"用户画像:{prof}")

    if req.glucose:
        gvals = [g.value_mmol for g in req.glucose]
        avg = sum(gvals) / len(gvals)
        lines.append(f"近期血糖共 {len(gvals)} 条,均值 {avg:.1f} mmol/L,"
                     f"最高 {max(gvals):.1f},最低 {min(gvals):.1f}。")
        recent = req.glucose[-8:]
        detail = "; ".join(f"{g.measured_at[5:16]} {g.period} {g.value_mmol}" for g in recent)
        lines.append(f"最近记录:{detail}")
        anomalies = detect(req.glucose)
        if anomalies:
            lines.append("检测到异常:" + "; ".join(a.message for a in anomalies[:6]))

    if req.diet:
        foods = "; ".join(
            f"{d.get('mealType', '')} {d.get('food', '')}"
            f"({d.get('calories', '?')}kcal)" for d in req.diet[:6])
        lines.append(f"近期饮食:{foods}")

    if req.exercise:
        ex = "; ".join(
            f"{e.get('type', '')} {e.get('durationMin', '?')}分钟"
            f"({e.get('intensity', '')})" for e in req.exercise[:6])
        lines.append(f"近期运动:{ex}")

    return "\n".join(lines) if lines else "用户暂无健康数据记录。"


def _build_messages(req: AdviceRequest) -> tuple[list[dict], str]:
    summary = _summarize(req)
    query = req.question or "糖尿病 血糖控制 饮食 运动 个性化建议"
    retrieved = retriever.retrieve(query, k=3)
    knowledge = "\n\n".join(f"【{r['source']}/{r['title']}】\n{r['text']}" for r in retrieved)

    task = req.question or "请根据我的数据生成一份综合的血糖管理方案(饮食调整 + 运动方案)。"
    user_content = (
        f"## 我的健康数据\n{summary}\n\n"
        f"## 可参考的医学知识\n{knowledge or '(无)'}\n\n"
        f"## 我的问题/需求\n{task}"
    )
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]
    context = f"数据摘要:\n{summary}\n\n检索知识:\n" + "; ".join(
        f"{r['source']}/{r['title']}(score={r['score']})" for r in retrieved)
    return messages, context


async def generate_advice(req: AdviceRequest) -> dict:
    messages, context = _build_messages(req)
    content = await deepseek_client.chat_content(messages)
    return {"content": content, "context": context}


async def stream_advice(req: AdviceRequest) -> AsyncIterator[str]:
    messages, _ = _build_messages(req)
    async for piece in deepseek_client.stream_chat(messages):
        yield piece
