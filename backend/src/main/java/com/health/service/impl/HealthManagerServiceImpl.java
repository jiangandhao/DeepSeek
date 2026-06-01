package com.health.service.impl;

import com.health.entity.HealthProfile;
import com.health.entity.RiskAssessment;
import com.health.mapper.HealthProfileMapper;
import com.health.mapper.RiskAssessmentMapper;
import com.health.service.HealthManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class HealthManagerServiceImpl implements HealthManagerService {

    @Autowired
    private HealthProfileMapper healthProfileMapper;

    @Autowired
    private RiskAssessmentMapper riskAssessmentMapper;

    @Override
    public HealthProfile getHealthProfile(Long userId) {
        return healthProfileMapper.selectByUserId(userId);
    }

    @Override
    public HealthProfile updateHealthProfile(HealthProfile profile) {
        profile.setUpdateTime(LocalDateTime.now());
        healthProfileMapper.updateById(profile);
        return profile;
    }

    @Override
    public Map<String, Object> assessRisk(Map<String, Object> assessmentData) {
        // 模拟AI风险评估逻辑
        Map<String, Object> result = new HashMap<>();

        int age = (int) assessmentData.getOrDefault("age", 30);
        double bmi = Double.parseDouble(assessmentData.getOrDefault("bmi", "24").toString());
        double bloodSugar = Double.parseDouble(assessmentData.getOrDefault("bloodSugar", "5.6").toString());

        int totalScore = 0;
        List<Map<String, Object>> risks = new ArrayList<>();

        // 计算风险分数
        if (bmi > 28) totalScore += 20;
        else if (bmi > 24) totalScore += 10;

        if (bloodSugar > 7.0) totalScore += 20;
        else if (bloodSugar > 6.1) totalScore += 10;

        // 生成疾病风险列表
        Map<String, Object> diabetesRisk = new HashMap<>();
        diabetesRisk.put("disease", "糖尿病");
        diabetesRisk.put("level", bloodSugar > 6.1 ? "高风险" : "低风险");
        diabetesRisk.put("probability", bloodSugar > 6.1 ? 75 : 20);
        risks.add(diabetesRisk);

        Map<String, Object> heartRisk = new HashMap<>();
        heartRisk.put("disease", "心血管疾病");
        heartRisk.put("level", totalScore > 30 ? "高风险" : totalScore > 15 ? "中风险" : "低风险");
        heartRisk.put("probability", Math.min(95, totalScore * 2 + 10));
        risks.add(heartRisk);

        result.put("score", totalScore);
        result.put("level", totalScore > 30 ? "high" : totalScore > 15 ? "medium" : "low");
        result.put("levelText", totalScore > 30 ? "高风险" : totalScore > 15 ? "中风险" : "低风险");
        result.put("risks", risks);

        // 保存评估记录
        RiskAssessment assessment = new RiskAssessment();
        assessment.setUserId(Long.parseLong(assessmentData.get("userId").toString()));
        assessment.setRiskScore((double) totalScore);
        assessment.setRiskLevel(totalScore > 30 ? 3 : totalScore > 15 ? 2 : 1);
        assessment.setCreateTime(LocalDateTime.now());
        riskAssessmentMapper.insert(assessment);

        return result;
    }

    @Override
    public List<RiskAssessment> getAssessmentHistory(Long userId) {
        return riskAssessmentMapper.selectByUserId(userId);
    }

    @Override
    public Map<String, Object> generateDietPrescription(Map<String, Object> params) {
        // 模拟AI生成饮食处方
        Map<String, Object> prescription = new HashMap<>();

        double weight = Double.parseDouble(params.getOrDefault("weight", "65").toString());
        String goal = params.getOrDefault("goal", "maintain").toString();

        int dailyCalories = "lose".equals(goal) ? 1500 : "gain".equals(goal) ? 2200 : 1800;

        prescription.put("totalCalories", dailyCalories);
        prescription.put("protein", (int)(weight * 1.2));
        prescription.put("carbs", (int)(dailyCalories * 0.5 / 4));
        prescription.put("fat", (int)(dailyCalories * 0.25 / 9));

        // 生成三餐建议
        List<Map<String, Object>> breakfast = new ArrayList<>();
        breakfast.add(createFoodItem("全麦面包", "2片", 150, "主食"));
        breakfast.add(createFoodItem("水煮蛋", "1个", 80, "蛋白质"));
        prescription.put("breakfast", breakfast);

        List<Map<String, Object>> lunch = new ArrayList<>();
        lunch.add(createFoodItem("糙米饭", "150g", 180, "主食"));
        lunch.add(createFoodItem("清蒸鱼", "150g", 180, "蛋白质"));
        prescription.put("lunch", lunch);

        return prescription;
    }

    @Override
    public Map<String, Object> generateExercisePlan(Map<String, Object> params) {
        // 模拟AI生成运动方案
        Map<String, Object> plan = new HashMap<>();

        String level = params.getOrDefault("level", "intermediate").toString();
        String goal = params.getOrDefault("goal", "general").toString();

        List<Map<String, Object>> weeklyPlan = new ArrayList<>();
        weeklyPlan.add(createDayPlan("周一", "有氧", "45分钟", 350));
        weeklyPlan.add(createDayPlan("周二", "力量", "60分钟", 280));
        weeklyPlan.add(createDayPlan("周三", "休息", "-", 0));
        weeklyPlan.add(createDayPlan("周四", "力量", "60分钟", 300));
        weeklyPlan.add(createDayPlan("周五", "有氧", "40分钟", 320));

        plan.put("weeklyPlan", weeklyPlan);
        plan.put("notes", "请根据自身感觉调整运动强度");

        return plan;
    }

    @Override
    public Map<String, Object> getDiseaseManagement(Long userId) {
        Map<String, Object> management = new HashMap<>();

        // 糖尿病管理
        Map<String, Object> diabetes = new HashMap<>();
        diabetes.put("diagnosedDate", "2023-06-15");
        diabetes.put("medication", "二甲双胍 500mg/次");
        diabetes.put("target", "空腹<7.0, 餐后<10.0");
        management.put("diabetes", diabetes);

        // 高血压管理
        Map<String, Object> hypertension = new HashMap<>();
        hypertension.put("diagnosedDate", "2022-03-10");
        hypertension.put("medication", "氨氯地平 5mg/次");
        hypertension.put("target", "<140/90 mmHg");
        management.put("hypertension", hypertension);

        return management;
    }

    @Override
    public Map<String, Object> addMonitoringRecord(Map<String, Object> record) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "记录保存成功");
        result.put("recordId", System.currentTimeMillis());
        return result;
    }

    @Override
    public List<Map<String, Object>> getMedicationReminder(Long userId) {
        List<Map<String, Object>> reminders = new ArrayList<>();
        reminders.add(createMedication("二甲双胍", "500mg", "每日2次", true));
        reminders.add(createMedication("氨氯地平", "5mg", "每日1次", true));
        reminders.add(createMedication("阿托伐他汀", "20mg", "每晚1次", false));
        return reminders;
    }

    @Override
    public List<Map<String, Object>> getFollowUpReminders(Long userId) {
        List<Map<String, Object>> reminders = new ArrayList<>();
        reminders.add(createFollowUp("心血管科复查", "2025-01-25", true));
        reminders.add(createFollowUp("内分泌科复查", "2025-02-15", false));
        return reminders;
    }

    @Override
    public List<Map<String, String>> getAiAdvice(Long userId) {
        List<Map<String, String>> advice = new ArrayList<>();
        advice.add(createAdvice("近期血糖控制良好，继续保持当前用药和饮食方案", "#67C23A"));
        advice.add(createAdvice("建议每周至少进行150分钟中等强度有氧运动", "#409EFF"));
        advice.add(createAdvice("血压偶有波动，注意情绪管理和规律作息", "#E6A23C"));
        return advice;
    }

    // 辅助方法
    private Map<String, Object> createFoodItem(String name, String amount, int calories, String category) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("amount", amount);
        item.put("calories", calories);
        item.put("category", category);
        return item;
    }

    private Map<String, Object> createDayPlan(String day, String type, String duration, int calories) {
        Map<String, Object> plan = new HashMap<>();
        plan.put("day", day);
        plan.put("type", type);
        plan.put("duration", duration);
        plan.put("calories", calories);
        return plan;
    }

    private Map<String, Object> createMedication(String name, String dosage, String frequency, boolean taken) {
        Map<String, Object> med = new HashMap<>();
        med.put("name", name);
        med.put("dosage", dosage);
        med.put("frequency", frequency);
        med.put("taken", taken);
        return med;
    }

    private Map<String, Object> createFollowUp(String type, String date, boolean urgent) {
        Map<String, Object> followUp = new HashMap<>();
        followUp.put("type", type);
        followUp.put("date", date);
        followUp.put("urgent", urgent);
        return followUp;
    }

    private Map<String, String> createAdvice(String text, String color) {
        Map<String, String> advice = new HashMap<>();
        advice.put("text", text);
        advice.put("color", color);
        return advice;
    }
}
