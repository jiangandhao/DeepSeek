"""体检报告、检验单与医学图片的通用结构化分析。"""
import io
import re
from pathlib import Path

import pytesseract
from PIL import Image, ImageOps
from pypdf import PdfReader

from app.imaging.detector import detect_nodules
from app.rag.retriever import retriever

DISCLAIMER = "AI 自动识别结果仅供健康管理参考，不构成诊断；请核对原报告并咨询执业医师。"

INDICATORS = [
    {"code": "GLU", "name": "空腹血糖", "aliases": ["空腹血糖", "葡萄糖", "GLU"], "low": 3.9, "high": 6.1, "unit": "mmol/L", "advice": "关注精制碳水和餐后活动，异常时复查空腹血糖及 HbA1c。"},
    {"code": "HBA1C", "name": "糖化血红蛋白", "aliases": ["糖化血红蛋白", "HbA1c", "HBA1C"], "low": 4.0, "high": 6.0, "unit": "%", "advice": "用于观察近 2–3 个月血糖状态，目标应结合个体风险。"},
    {"code": "TC", "name": "总胆固醇", "aliases": ["总胆固醇", "TC"], "low": 0.0, "high": 5.2, "unit": "mmol/L", "advice": "减少反式脂肪和过多饱和脂肪，结合 LDL-C 评估。"},
    {"code": "TG", "name": "甘油三酯", "aliases": ["甘油三酯", "TG"], "low": 0.0, "high": 1.7, "unit": "mmol/L", "advice": "控制酒精、含糖饮料和总能量摄入。"},
    {"code": "LDL", "name": "低密度脂蛋白", "aliases": ["低密度脂蛋白", "LDL-C", "LDL"], "low": 0.0, "high": 3.4, "unit": "mmol/L", "advice": "心血管高危人群目标更严格，建议结合整体风险咨询医生。"},
    {"code": "HDL", "name": "高密度脂蛋白", "aliases": ["高密度脂蛋白", "HDL-C", "HDL"], "low": 1.0, "high": 99.0, "unit": "mmol/L", "advice": "规律运动、戒烟和体重管理有助于改善血脂结构。"},
    {"code": "ALT", "name": "丙氨酸氨基转移酶", "aliases": ["丙氨酸氨基转移酶", "谷丙转氨酶", "ALT"], "low": 0.0, "high": 40.0, "unit": "U/L", "advice": "避免饮酒和自行使用伤肝药物，持续异常需进一步评估。"},
    {"code": "AST", "name": "天门冬氨酸氨基转移酶", "aliases": ["天门冬氨酸氨基转移酶", "谷草转氨酶", "AST"], "low": 0.0, "high": 40.0, "unit": "U/L", "advice": "结合 ALT、症状及近期运动情况综合判断。"},
    {"code": "CREA", "name": "肌酐", "aliases": ["肌酐", "CREA", "Cr"], "low": 44.0, "high": 106.0, "unit": "μmol/L", "advice": "结合年龄、肌肉量和 eGFR 评估肾功能。"},
    {"code": "UA", "name": "尿酸", "aliases": ["尿酸", "UA"], "low": 90.0, "high": 420.0, "unit": "μmol/L", "advice": "注意饮水与高嘌呤食物，持续升高或关节痛时就医。"},
    {"code": "HGB", "name": "血红蛋白", "aliases": ["血红蛋白", "HGB"], "low": 115.0, "high": 175.0, "unit": "g/L", "advice": "异常可能与贫血、脱水等相关，应结合性别和报告范围。"},
]


def _extract_text(content: bytes, filename: str, content_type: str | None) -> tuple[str, str]:
    suffix = Path(filename or "").suffix.lower()
    if suffix == ".pdf" or content_type == "application/pdf":
        reader = PdfReader(io.BytesIO(content))
        return "\n".join(page.extract_text() or "" for page in reader.pages), "PDF_TEXT"
    image = Image.open(io.BytesIO(content)).convert("RGB")
    image = ImageOps.autocontrast(ImageOps.grayscale(image))
    text = pytesseract.image_to_string(image, lang="chi_sim+eng", config="--psm 6")
    return text, "IMAGE_OCR"


def _parse_indicators(text: str) -> list[dict]:
    normalized = text.replace("，", ",").replace("：", ":")
    results = []
    for item in INDICATORS:
        match = None
        for alias in sorted(item["aliases"], key=len, reverse=True):
            boundary = rf"(?<![A-Za-z0-9]){re.escape(alias)}(?![A-Za-z0-9])" if alias.isascii() else re.escape(alias)
            pattern = rf"(?i){boundary}[^\d<>-]{{0,24}}([<>]?\s*-?\d+(?:\.\d+)?)"
            match = re.search(pattern, normalized)
            if match:
                break
        if not match:
            continue
        raw = match.group(1).replace(" ", "").lstrip("<>")
        try:
            value = float(raw)
        except ValueError:
            continue
        status = "LOW" if value < item["low"] else "HIGH" if value > item["high"] else "NORMAL"
        results.append({"code": item["code"], "name": item["name"], "value": value,
                        "unit": item["unit"], "reference": f"{item['low']:g}–{item['high']:g}",
                        "status": status, "advice": item["advice"]})
    return results


def analyze_report(content: bytes, filename: str, content_type: str | None = None) -> dict:
    if not content:
        raise ValueError("文件内容为空")
    try:
        text, method = _extract_text(content, filename, content_type)
    except Exception as exc:
        raise ValueError(f"无法解析该文件：{exc}") from exc

    indicators = _parse_indicators(text)
    abnormal = [item for item in indicators if item["status"] != "NORMAL"]
    keywords = " ".join(item["name"] for item in abnormal or indicators)
    evidence = retriever.retrieve(f"体检报告 {keywords} 解读 改善建议", k=4)

    if len(text.strip()) < 12 and (content_type or "").startswith("image/"):
        imaging = detect_nodules(content)
        return {"report_type": "MEDICAL_IMAGE", "extraction_method": "IMAGE_ANALYSIS",
                "title": "医学影像辅助分析", "risk_level": imaging["level"],
                "summary": imaging["summary"], "indicators": [], "abnormal_count": imaging["count"],
                "imaging": imaging, "knowledge": evidence, "raw_text": "", "disclaimer": DISCLAIMER}

    risk = "HIGH" if len(abnormal) >= 3 else "MEDIUM" if abnormal else "LOW"
    if abnormal:
        names = "、".join(f"{item['name']}({item['status']})" for item in abnormal[:5])
        summary = f"共识别 {len(indicators)} 项常见指标，其中 {len(abnormal)} 项需要关注：{names}。"
    elif indicators:
        summary = f"已识别 {len(indicators)} 项常见指标，未发现超出内置通用参考范围的项目。"
    else:
        summary = "已完成文本提取，但未可靠匹配到内置常见指标，请对照原报告或使用更清晰的图片。"
    return {"report_type": "LAB_REPORT" if indicators else "GENERAL_EXAM_REPORT",
            "extraction_method": method, "title": "体检报告智能解读", "risk_level": risk,
            "summary": summary, "indicators": indicators, "abnormal_count": len(abnormal),
            "knowledge": evidence, "raw_text": text[:8000], "disclaimer": DISCLAIMER}
