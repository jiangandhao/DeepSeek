from app.plans.generator import generate_structured_plan
from app.agents.health_manager import _fallback_adjustment
from app.schemas import AdviceRequest


def test_structured_plan_has_meals_and_seven_days(glucose_series):
    request = AdviceRequest(profile={"age": 46, "weight_kg": 70, "chronic": "高血压"},
                            glucose=glucose_series)
    plan = generate_structured_plan(request)
    assert len(plan["meals"]) == 4
    assert all(len(meal["alternatives"]) >= 4 for meal in plan["meals"])
    assert all({"name", "foods", "calories", "carbs_g", "gl"} <= set(meal["alternatives"][0])
               for meal in plan["meals"])
    assert len(plan["exercise_week"]) == 7
    assert plan["daily_targets"]["sodium_mg_max"] == 1500
    assert "disclaimer" in plan


def test_joint_issue_uses_low_impact_activity():
    request = AdviceRequest(profile={"age": 60, "weight_kg": 65, "chronic": "膝关节炎"})
    plan = generate_structured_plan(request)
    aerobic = [day for day in plan["exercise_week"] if day["day"] == 1][0]
    assert "自行车" in aerobic["type"] or "游泳" in aerobic["type"]


def test_adjustment_fallback_is_actionable_for_dinner_party():
    request = AdviceRequest(question="今晚临时聚餐，应该怎么调整？")
    content = _fallback_adjustment(request, {"level": "LOW"})
    assert "80–100g" in content
    assert "餐后" in content
    assert "本地知识库" in content
