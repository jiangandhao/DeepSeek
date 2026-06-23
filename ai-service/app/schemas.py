"""请求 / 响应数据模型。"""
from typing import Literal, Optional
from pydantic import BaseModel, Field


# ---- Mimo 通用对话 ----
class ChatMessage(BaseModel):
    role: Literal["system", "user", "assistant"]
    content: str


class ChatRequest(BaseModel):
    messages: list[ChatMessage]
    temperature: float = 0.7
    stream: bool = False


class ChatResponse(BaseModel):
    content: str


# ---- 血糖记录(用于预测 / 智能体输入) ----
class GlucancePoint(BaseModel):
    value_mmol: float
    period: str
    measured_at: str  # ISO 时间字符串


# ---- 血糖预测(阶段2) ----
class PredictRequest(BaseModel):
    history: list[GlucancePoint] = Field(..., description="历史血糖序列(按时间升序)")
    horizon: int = Field(6, description="预测未来多少个点")


class PredictPoint(BaseModel):
    value_mmol: float
    measured_at: str


class PredictResponse(BaseModel):
    predictions: list[PredictPoint]
    model: str
    metrics: Optional[dict] = None


# ---- 异常检测(阶段2) ----
class AnomalyRequest(BaseModel):
    history: list[GlucancePoint]


class AnomalyItem(BaseModel):
    measured_at: str
    value_mmol: float
    category: str  # GLUCOSE_HIGH / GLUCOSE_LOW / FLUCTUATION
    level: str     # LOW / MEDIUM / HIGH
    message: str


class AnomalyResponse(BaseModel):
    anomalies: list[AnomalyItem]


# ---- 血糖管理智能体(阶段3) ----
class AdviceRequest(BaseModel):
    """由 Java 后端汇总用户数据后传入。"""
    profile: dict = Field(default_factory=dict, description="用户画像:年龄/性别/BMI/糖尿病类型等")
    glucose: list[GlucancePoint] = Field(default_factory=list)
    diet: list[dict] = Field(default_factory=list)
    exercise: list[dict] = Field(default_factory=list)
    question: Optional[str] = Field(None, description="用户的具体提问,为空则生成综合方案")
    stream: bool = False


class AdviceResponse(BaseModel):
    content: str
    context: str


# ---- 疾病风险评估(阶段4) ----
class RiskRequest(BaseModel):
    profile: dict = Field(default_factory=dict)
    glucose: list[GlucancePoint] = Field(default_factory=list)


class RiskResponse(BaseModel):
    level: str           # LOW / MEDIUM / HIGH
    score: int
    factors: list[str]
    metrics: dict
    message: str
