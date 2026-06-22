"""基于用户画像约束生成可直接供前端渲染的结构化饮食与运动计划。"""
from app.rag.retriever import retriever
from app.risk.scorer import score_risk
from app.schemas import AdviceRequest

DISCLAIMER = "AI 生成，仅供健康管理参考，不替代专业医疗意见；涉及用药和治疗请咨询执业医师。"


def _dish(name: str, foods: list[tuple[str, str]], calories: int, carbs: float, gl: float) -> dict:
    return {"name": name, "foods": [{"name": food, "amount": amount} for food, amount in foods],
            "calories": calories, "carbs_g": carbs, "gl": gl}


def _number(profile: dict, *keys: str, default: float) -> float:
    for key in keys:
        try:
            if profile.get(key) is not None:
                return float(profile[key])
        except (TypeError, ValueError):
            pass
    return default


def generate_structured_plan(req: AdviceRequest) -> dict:
    profile = req.profile
    age = int(_number(profile, "age", "年龄", default=40))
    weight = _number(profile, "weight_kg", "体重", default=65)
    chronic = str(profile.get("chronic", ""))
    diabetes = str(profile.get("糖尿病类型", "无")) != "无" or "糖尿病" in chronic
    hypertension = "高血压" in chronic
    joint_issue = any(word in chronic for word in ("膝", "关节", "腰椎"))
    calories = max(1300, min(2400, round(weight * (24 if diabetes else 27) / 50) * 50))
    sodium = 1500 if hypertension else 2000
    heart_low, heart_high = round((220 - age) * 0.55), round((220 - age) * 0.7)
    risk = score_risk(profile, req.glucose)

    meal_specs = [
        ("BREAKFAST", "08:00", _dish("燕麦鸡蛋能量早餐", [("燕麦", "40g"), ("鸡蛋", "1个"), ("无糖酸奶", "150ml")], round(calories * .25), 42, 9), [
            _dish("荞麦鸡蛋蔬菜面", [("荞麦面", "60g"), ("鸡蛋", "1个"), ("青菜", "150g")], 405, 48, 10),
            _dish("小米杂豆粥配豆腐干", [("小米杂豆粥", "250ml"), ("豆腐干", "50g"), ("小番茄", "100g")], 380, 46, 8),
            _dish("全麦牛油果鸡蛋卷", [("全麦饼", "1张"), ("鸡蛋", "1个"), ("牛油果", "40g")], 430, 39, 7),
            _dish("无糖酸奶莓果杯", [("无糖酸奶", "200ml"), ("蓝莓", "80g"), ("坚果", "15g"), ("燕麦", "25g")], 390, 41, 7)]),
        ("LUNCH", "12:00", _dish("藜麦鸡胸时蔬碗", [("鸡胸肉", "100g"), ("藜麦杂粮饭", "100g"), ("非淀粉蔬菜", "250g")], round(calories * .35), 55, 12), [
            _dish("清蒸鱼配糙米", [("鲈鱼", "120g"), ("糙米饭", "100g"), ("西兰花", "220g")], 520, 48, 10),
            _dish("菌菇豆腐杂粮碗", [("北豆腐", "120g"), ("菌菇", "100g"), ("杂粮饭", "80g"), ("绿叶菜", "180g")], 480, 44, 9),
            _dish("番茄牛肉荞麦面", [("瘦牛肉", "90g"), ("荞麦面", "70g"), ("番茄", "180g")], 535, 52, 11),
            _dish("虾仁鹰嘴豆沙拉", [("虾仁", "100g"), ("鹰嘴豆", "80g"), ("混合蔬菜", "250g"), ("全麦面包", "1片")], 495, 43, 8)]),
        ("DINNER", "18:00", _dish("清蒸鱼配双蔬", [("鱼或豆腐", "120g"), ("绿叶蔬菜", "250g"), ("杂粮饭", "80g")], round(calories * .30), 42, 10), [
            _dish("豆腐菌菇煲", [("北豆腐", "150g"), ("菌菇", "120g"), ("时蔬", "200g"), ("糙米饭", "70g")], 430, 38, 8),
            _dish("去皮鸡腿配烤蔬菜", [("去皮鸡腿", "120g"), ("烤彩蔬", "250g"), ("红薯", "100g")], 465, 41, 9),
            _dish("番茄豆腐虾仁汤", [("虾仁", "90g"), ("豆腐", "100g"), ("番茄", "180g"), ("玉米", "80g")], 410, 36, 8),
            _dish("鸡丝杂蔬荞麦冷面", [("鸡胸肉", "90g"), ("荞麦面", "60g"), ("杂蔬", "220g")], 445, 47, 10)]),
        ("SNACK", "15:30", _dish("坚果水果加餐", [("坚果", "10g"), ("低糖水果", "100g")], round(calories * .10), 15, 5), [
            _dish("无糖酸奶", [("无糖酸奶", "150ml")], 95, 8, 3),
            _dish("小番茄配奶酪", [("小番茄", "150g"), ("低脂奶酪", "20g")], 110, 9, 2),
            _dish("苹果配坚果", [("苹果", "100g"), ("杏仁", "8g")], 105, 16, 5),
            _dish("低糖豆浆", [("无糖豆浆", "250ml")], 90, 7, 2)])
    ]
    meals = []
    for meal_type, time, primary, alternatives in meal_specs:
        meals.append({"meal_type": meal_type, "time": time, **primary,
                      "alternatives": alternatives})
    activity = "固定自行车或游泳" if joint_issue else "餐后快走"
    week = []
    for day in range(1, 8):
        if day in (2, 5):
            kind, duration, intensity = "抗阻训练", 25, "MEDIUM"
        elif day == 7:
            kind, duration, intensity = "拉伸与主动恢复", 20, "LOW"
        else:
            kind, duration, intensity = activity, 30, "MEDIUM"
        week.append({"day": day, "type": kind, "duration_min": duration, "intensity": intensity,
                     "heart_rate": f"{heart_low}–{heart_high}", "precaution": "不适时立即停止，循序渐进。"})

    query = f"{chronic} {diabetes} 饮食 运动 营养 处方"
    return {"version": "1.0", "risk": risk, "daily_targets": {"calories": calories,
            "carbohydrate_percent": 45, "protein_percent": 25, "fat_percent": 30,
            "fiber_g": 25, "sodium_mg_max": sodium, "water_ml": round(weight * 30)},
            "meals": meals, "exercise_week": week,
            "monitoring": ["记录空腹与餐后血糖变化", "运动前后关注不适和低血糖信号",
                           "每周复盘体重、执行率与睡眠"],
            "knowledge": retriever.retrieve(query, k=4), "disclaimer": DISCLAIMER}
