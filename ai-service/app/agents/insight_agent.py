"""结构化洞察智能体。

让 LLM 针对某一方面(血糖/风险/饮食/运动/综合或自由问题)输出**固定 schema 的 JSON**,
前端可直接渲染成漂亮的卡片(总览 + 关键指标 + 分节 + 建议)。解析失败时回退为 Markdown。
复用 glucose_agent 的数据摘要与 RAG 检索。
"""
import json
from typing import Optional

import httpx

from app.agents.glucose_agent import _summarize
from app.deepseek.client import deepseek_client
from app.rag.retriever import retriever
from app.schemas import InsightRequest

DISCLAIMER = "本分析由 AI 生成，仅供健康管理参考，不替代专业医疗意见；涉及用药与治疗请咨询执业医师。"

ASPECT_FOCUS = {
    "glucose": "请聚焦【血糖管理】：血糖水平与波动、达标情况、餐前餐后/空腹差异、低/高血糖风险。",
    "risk": "请聚焦【慢病风险评估】：当前风险等级与主要风险因素、需要警惕的指标与趋势。",
    "diet": "请聚焦【饮食营养】：能量与碳水结构、进餐顺序与搭配、需要调整的饮食习惯。",
    "exercise": "请聚焦【运动方案】：运动类型/强度/频率/时机，结合血糖与身体状况的安全建议。",
    "comprehensive": "请做【综合健康分析】：覆盖血糖、饮食、运动与整体趋势，给出全局判断。",
}

SYSTEM_PROMPT = (
    "你是一名专业的糖尿病健康管理 AI 分析师。请基于用户真实数据与【医学知识】做分析，"
    "并【只输出一个 JSON 对象】，不要输出任何额外文字、解释或代码围栏之外的内容。\n"
    "JSON 必须严格符合以下结构：\n"
    "{\n"
    '  "title": "分析标题(简短)",\n'
    '  "summary": "一句话总览结论",\n'
    '  "metrics": [{"label":"指标名","value":"数值或文字","unit":"单位(可空)","trend":"up|down|flat","status":"good|warn|danger"}],\n'
    '  "sections": [{"heading":"小节标题","body":"该小节正文，可用 Markdown 列表/加粗"}],\n'
    '  "suggestions": ["可执行建议1","建议2"],\n'
    '  "disclaimer": "免责声明"\n'
    "}\n"
    "要求：metrics 给 2-4 个最关键指标；sections 给 2-4 节；suggestions 给 3-5 条具体可执行项；"
    "语言通俗、专业而温和；不开具处方药剂量，涉及用药提示遵医嘱。"
)

# 流式输出用:产出富 Markdown 报告(非 JSON),逐字流式渲染。
STREAM_SYSTEM_PROMPT = (
    "你是一名专业的糖尿病健康管理 AI 分析师。请基于用户的真实数据并参考【医学知识】，"
    "针对指定方面给出详尽、专业而通俗的分析。要求：\n"
    "1. 使用 Markdown，分节呈现（## 小标题 + 列表/加粗），层次清晰；\n"
    "2. 覆盖【现状解读】【关键发现与风险】【可执行建议(饮食/运动/监测)】；\n"
    "3. 结合数据中的异常点与趋势做针对性说明；\n"
    "4. 不开具处方药剂量，涉及用药调整时提示遵医嘱；\n"
    "5. 语气专业而温和，避免制造焦虑。"
)


def _build_messages(req: InsightRequest) -> tuple[list[dict], str]:
    summary = _summarize(req)
    focus = ASPECT_FOCUS.get((req.aspect or "").lower(), "")
    query = req.question or f"糖尿病 {req.aspect or '综合'} 血糖 饮食 运动 个性化建议"
    retrieved = retriever.retrieve(query, k=3)
    knowledge = "\n\n".join(f"【{r['source']}/{r['title']}】\n{r['text']}" for r in retrieved)

    task = req.question or "请根据我的数据给出这一方面的结构化分析。"
    user_content = (
        f"## 我的健康数据\n{summary}\n\n"
        f"## 分析重点\n{focus or '综合分析我的健康数据。'}\n\n"
        f"## 可参考的医学知识\n{knowledge or '(无)'}\n\n"
        f"## 我的问题/需求\n{task}\n\n"
        "请按系统要求只输出 JSON。"
    )
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]
    context = "检索知识:" + "; ".join(
        f"{r['source']}/{r['title']}" for r in retrieved)
    return messages, context


def _extract_json(text: str) -> Optional[dict]:
    """从模型输出中尽量提取 JSON 对象(容忍 ```json 围栏与前后噪声)。"""
    if not text:
        return None
    s = text.strip()
    if s.startswith("```"):
        s = s.strip("`")
        if s[:4].lower() == "json":
            s = s[4:]
    start, end = s.find("{"), s.rfind("}")
    if start == -1 or end == -1 or end <= start:
        return None
    try:
        data = json.loads(s[start:end + 1])
        return data if isinstance(data, dict) else None
    except json.JSONDecodeError:
        return None


def _fallback(raw: str, context: str) -> dict:
    """LLM 不可用或非 JSON 输出时,保证前端仍有内容可渲染。"""
    body = raw.strip() if raw and raw.strip() else "暂时无法生成分析，请稍后重试或检查模型配置。"
    return {
        "title": "AI 健康分析",
        "summary": "",
        "metrics": [],
        "sections": [{"heading": "分析", "body": body}],
        "suggestions": [],
        "disclaimer": DISCLAIMER,
        "_fallback": True,
        "context": context,
    }


def _normalize(data: dict, context: str) -> dict:
    """补全缺省字段,确保结构稳定。"""
    data.setdefault("title", "AI 健康分析")
    data.setdefault("summary", "")
    data.setdefault("metrics", [])
    data.setdefault("sections", [])
    data.setdefault("suggestions", [])
    data.setdefault("disclaimer", DISCLAIMER)
    data["context"] = context
    return data


async def generate_insight(req: InsightRequest) -> dict:
    messages, context = _build_messages(req)
    try:
        raw = await deepseek_client.chat_content(messages, temperature=0.3)
    except httpx.HTTPError as e:
        return _fallback("", f"{context}\n[模型调用失败:{e}]")
    parsed = _extract_json(raw)
    if parsed is None:
        return _fallback(raw, context)
    return _normalize(parsed, context)


def _build_stream_messages(req: InsightRequest) -> list[dict]:
    """流式分析用:产出富 Markdown(非 JSON)。"""
    summary = _summarize(req)
    focus = ASPECT_FOCUS.get((req.aspect or "").lower(), "")
    query = req.question or f"糖尿病 {req.aspect or '综合'} 血糖 饮食 运动 个性化建议"
    retrieved = retriever.retrieve(query, k=3)
    knowledge = "\n\n".join(f"【{r['source']}/{r['title']}】\n{r['text']}" for r in retrieved)
    task = req.question or "请根据我的数据给出这一方面的详细分析与建议。"
    user_content = (
        f"## 我的健康数据\n{summary}\n\n"
        f"## 分析重点\n{focus or '综合分析我的健康数据。'}\n\n"
        f"## 可参考的医学知识\n{knowledge or '(无)'}\n\n"
        f"## 我的问题/需求\n{task}"
    )
    return [
        {"role": "system", "content": STREAM_SYSTEM_PROMPT},
        {"role": "user", "content": user_content},
    ]


async def stream_insight(req: InsightRequest):
    """流式产出 Markdown 分析,逐字 yield。"""
    messages = _build_stream_messages(req)
    async for piece in deepseek_client.stream_chat(messages):
        yield piece
