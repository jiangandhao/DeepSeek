from fastapi import APIRouter, File, HTTPException, UploadFile

from app.reports.analyzer import analyze_report

router = APIRouter(prefix="/api/reports", tags=["reports"])


@router.post("/analyze")
async def analyze(file: UploadFile = File(...)):
    """解析体检报告图片/PDF或辅助分析医学图片。"""
    content = await file.read()
    try:
        return analyze_report(content, file.filename or "report", file.content_type)
    except ValueError as exc:
        raise HTTPException(status_code=400, detail=str(exc)) from exc
