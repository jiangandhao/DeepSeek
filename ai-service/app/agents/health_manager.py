"""AI 数智健管师编排。

在血糖智能体基础上,综合用户画像/家族史 + 风险评分,生成:
风险评估解读 + 个性化饮食处方 + 运动计划调整。
复用 glucose_agent 的数据摘要与 RAG 检索。
"""
from typing import AsyncIterator

import httpx

from app.agents.glucose_agent import _summarize
from app.deepseek.client import deepseek_client
from app.rag.retriever import retriever
from app.risk.scorer import score_risk
from app.schemas import AdviceRequest

SYSTEM_PROMPT = (
    "你是一名资深的 AI 数智健管师,擅长慢病(尤其糖尿病)风险评估与健康管理。"
    "请综合用户的画像/家族史、血糖/饮食/运动数据以及【风险评估结果】和【医学知识】,"
    "输出一份专业、个性化、可执行的健康管理方案,包含:\n"
    "1. **风险解读**:解释当前风险等级与主要风险因素;\n"
    "2. **饮食处方**:具体的营养搭配建议(主食/蛋白/蔬菜/进餐顺序);\n"
    "3. **运动计划**:类型、强度、频率、时机的个性化推荐;\n"
    "4. **监测与随访建议**。\n"
    "要求 Markdown 分节、通俗易懂;不开具处方药剂量,涉及用药请提示遵医嘱;语气专业而温和。"
)


def _build_messages(req: AdviceRequest) -> tuple[list[dict], str, dict]:
    risk = score_risk(req.profile, req.glucose)
    summary = _summarize(req)

    query = req.question or "糖尿病 风险评估 个性化 饮食处方 运动计划 并发症预防"
    retrieved = retriever.retrieve(query, k=3)
    knowledge = "\n\n".join(f"【{r['source']}/{r['title']}】\n{r['text']}" for r in retrieved)

    risk_text = (f"风险等级:{risk['level']},评分:{risk['score']}。"
                 f"风险因素:{'、'.join(risk['factors']) or '无明显因素'}。"
                 f"关键指标:{risk['metrics']}")

    task = req.question or "请基于以上信息,给出我的疾病风险解读与个性化健康管理方案。"
    user_content = (
        f"## 我的健康数据\n{summary}\n\n"
        f"## 风险评估结果\n{risk_text}\n\n"
        f"## 可参考的医学知识\n{knowledge or '(无)'}\n\n"
        f"## 我的需求\n{task}"
    )
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]
    context = f"风险:{risk_text}\n数据摘要:\n{summary}\n检索:" + "; ".join(
        f"{r['source']}/{r['title']}" for r in retrieved)
    return messages, context, risk


async def generate_health_plan(req: AdviceRequest) -> dict:
    messages, context, risk = _build_messages(req)
    try:
        content = await deepseek_client.chat_content(messages)
        return {"content": content, "context": "mimo\n" + context, "risk": risk,
                "provider": "mimo"}
    except httpx.HTTPError:
        content = _fallback_adjustment(req, risk)
        return {"content": content, "context": "local-rag-fallback\n" + context,
                "risk": risk, "provider": "local-rag-fallback"}


def _fallback_adjustment(req: AdviceRequest, risk: dict) -> str:
    """外部模型不可用时，依据本地向量知识与安全规则提供可执行建议。"""
    question = req.question or "生成综合健康管理方案"
    evidence = retriever.retrieve(question + " 饮食 运动 血糖", k=3)
    sources = "、".join(f"{item['source']}/{item['title']}" for item in evidence) or "内置健康管理规则"
    if any(word in question for word in ("聚餐", "外卖", "宴请")):
        action = (
            "1. **聚餐前**：不要空腹赴宴，可提前吃 100–150g 无淀粉蔬菜或一杯无糖酸奶。\n"
            "2. **进餐时**：按蔬菜→蛋白质→主食顺序；主食控制在熟重 80–100g，含糖饮料和酒尽量不选。\n"
            "3. **菜品选择**：优先清蒸、炖、白灼；油炸或浓汁菜控制在平时一半分量。\n"
            "4. **餐后活动**：无运动禁忌时，餐后 20–30 分钟开始快走 20–30 分钟。"
        )
    elif any(word in question for word in ("替换", "换成", "不吃", "过敏")):
        action = (
            "1. 保持同类交换：主食换主食、优质蛋白换优质蛋白，避免只删除不补足。\n"
            "2. 每份主食替换目标约 25–30g 碳水；鱼肉 100g 可换北豆腐约 150g 或鸡胸肉 90–100g。\n"
            "3. 替换后继续保留至少 200g 非淀粉类蔬菜，并观察餐后血糖反馈。"
        )
    else:
        action = (
            "1. 每餐保证蔬菜、优质蛋白和定量主食，减少含糖饮料及精制点心。\n"
            "2. 无禁忌时每周累计至少 150 分钟中等强度有氧活动，并安排 2 次抗阻训练。\n"
            "3. 连续记录 3 天执行情况与餐后血糖，再根据趋势调整主食 10–15g。"
        )
    return (
        "## 本次调整建议\n" + action + "\n\n"
        f"## 风险与监测\n当前规则风险等级为 **{risk['level']}**。如出现低血糖、胸痛、明显呼吸困难或其他急性不适，应停止运动并及时就医。\n\n"
        f"## 参考依据\n本次降级建议检索自：{sources}。Mimo 暂不可用时系统已自动切换本地知识库。\n\n"
        "> 本建议仅供健康管理参考，不替代执业医师或营养师意见。"
    )


async def stream_health_plan(req: AdviceRequest) -> AsyncIterator[str]:
    messages, _, _ = _build_messages(req)
    async for piece in deepseek_client.stream_chat(messages):
        yield piece
