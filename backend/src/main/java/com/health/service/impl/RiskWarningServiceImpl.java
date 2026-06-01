package com.health.service.impl;

import com.health.entity.WarningRecord;
import com.health.entity.WarningThreshold;
import com.health.entity.HealthGoal;
import com.health.mapper.WarningRecordMapper;
import com.health.mapper.WarningThresholdMapper;
import com.health.mapper.HealthGoalMapper;
import com.health.service.RiskWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RiskWarningServiceImpl implements RiskWarningService {

    @Autowired
    private WarningRecordMapper warningRecordMapper;

    @Autowired
    private WarningThresholdMapper warningThresholdMapper;

    @Autowired
    private HealthGoalMapper healthGoalMapper;

    @Override
    public Map<String, Object> getRiskAssessment(Long userId) {
        Map<String, Object> assessment = new HashMap<>();
        assessment.put("userId", userId);
        assessment.put("totalScore", 45);
        assessment.put("level", "medium");
        assessment.put("levelText", "中风险");

        List<Map<String, Object>> risks = new ArrayList<>();
        risks.add(createRisk("2型糖尿病", "高风险", 75, Arrays.asList("血糖偏高", "家族史")));
        risks.add(createRisk("心血管疾病", "中风险", 45, Arrays.asList("血脂异常", "轻度肥胖")));
        risks.add(createRisk("高血压", "中风险", 40, Arrays.asList("血压偏高")));
        assessment.put("risks", risks);

        return assessment;
    }

    @Override
    public Map<String, Object> submitAssessment(Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("assessmentId", System.currentTimeMillis());
        result.put("message", "评估完成");
        return result;
    }

    @Override
    public Map<String, Object> getRiskDetail(Long userId, String diseaseType) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("disease", diseaseType);
        detail.put("level", "中风险");
        detail.put("probability", 45);
        detail.put("factors", Arrays.asList("血糖偏高", "BMI偏高", "家族史"));
        detail.put("suggestions", Arrays.asList("控制饮食", "增加运动", "定期监测"));
        return detail;
    }

    @Override
    public Map<String, Object> getRiskLevels(Long userId) {
        Map<String, Object> levels = new HashMap<>();
        levels.put("high", 1);
        levels.put("medium", 2);
        levels.put("low", 5);

        List<Map<String, Object>> diseaseRisks = new ArrayList<>();
        diseaseRisks.add(createDiseaseRisk("2型糖尿病", "高风险", 75));
        diseaseRisks.add(createDiseaseRisk("心血管疾病", "中风险", 45));
        diseaseRisks.add(createDiseaseRisk("高血压", "中风险", 40));
        diseaseRisks.add(createDiseaseRisk("脂肪肝", "低风险", 25));
        levels.put("diseaseRisks", diseaseRisks);

        return levels;
    }

    @Override
    public List<Map<String, Object>> getRiskFactors(Long userId) {
        List<Map<String, Object>> factors = new ArrayList<>();
        factors.add(createFactor("血糖水平", 85, true, "#F56C6C", "空腹血糖6.8，高于正常值"));
        factors.add(createFactor("BMI指数", 70, true, "#E6A23C", "BMI 25.4，属于超重范围"));
        factors.add(createFactor("家族病史", 60, false, "#909399", "父母有糖尿病史"));
        factors.add(createFactor("运动习惯", 45, true, "#67C23A", "每周运动不足2次"));
        return factors;
    }

    @Override
    public List<Map<String, Object>> getWarnings(Long userId, String level, Boolean read) {
        List<WarningRecord> records = warningRecordMapper.selectByUserId(userId);
        List<Map<String, Object>> warnings = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (WarningRecord record : records) {
            if (record.getIsDismissed() != null && record.getIsDismissed() == 1) {
                continue;
            }
            if (level != null && !level.isEmpty() && !level.equals(record.getWarningLevel())) {
                continue;
            }
            if (read != null) {
                boolean isRead = record.getIsRead() != null && record.getIsRead() == 1;
                if (read != isRead) {
                    continue;
                }
            }
            Map<String, Object> warning = new HashMap<>();
            warning.put("id", record.getId());
            warning.put("level", record.getWarningLevel());
            warning.put("title", record.getTitle());
            warning.put("content", record.getContent());
            warning.put("read", record.getIsRead() != null && record.getIsRead() == 1);
            warning.put("time", record.getCreateTime() != null ? record.getCreateTime().format(formatter) : "");
            warnings.add(warning);
        }
        return warnings;
    }

    @Override
    public Map<String, Object> markWarningRead(Long id) {
        int count = warningRecordMapper.markRead(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", count > 0);
        result.put("message", count > 0 ? "已标记为已读" : "预警记录不存在");
        return result;
    }

    @Override
    public Map<String, Object> markAllWarningsRead(Long userId) {
        int count = warningRecordMapper.markAllRead(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", count);
        result.put("message", "已全部标记为已读");
        return result;
    }

    @Override
    public Map<String, Object> dismissWarning(Long id) {
        int count = warningRecordMapper.dismiss(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", count > 0);
        result.put("message", count > 0 ? "已忽略" : "预警记录不存在");
        return result;
    }

    @Override
    public Map<String, Object> getWarningStats(Long userId) {
        List<WarningRecord> records = warningRecordMapper.selectByUserId(userId);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);
        int todayCount = 0;
        int weekCount = 0;
        int unreadCount = 0;
        int resolvedCount = 0;
        for (WarningRecord record : records) {
            boolean isRead = record.getIsRead() != null && record.getIsRead() == 1;
            boolean isDismissed = record.getIsDismissed() != null && record.getIsDismissed() == 1;
            if (!isRead) {
                unreadCount++;
            }
            if (isDismissed) {
                resolvedCount++;
            }
            if (record.getCreateTime() != null) {
                LocalDate createDate = record.getCreateTime().toLocalDate();
                if (!createDate.isBefore(today) && !createDate.isAfter(today)) {
                    todayCount++;
                }
                if (!createDate.isBefore(weekStart) && !createDate.isAfter(today)) {
                    weekCount++;
                }
            }
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("today", todayCount);
        stats.put("week", weekCount);
        stats.put("unread", unreadCount);
        stats.put("resolved", resolvedCount);
        return stats;
    }

    @Override
    public Map<String, Object> getWarningThresholds(Long userId) {
        List<WarningThreshold> records = warningThresholdMapper.selectByUserId(userId);
        Map<String, Object> thresholds = new HashMap<>();
        for (WarningThreshold record : records) {
            thresholds.put(record.getThresholdType(), record.getThresholdValue());
        }
        return thresholds;
    }

    @Override
    public Map<String, Object> updateWarningThresholds(Long userId, Map<String, Object> thresholds) {
        List<WarningThreshold> existing = warningThresholdMapper.selectByUserId(userId);
        Map<String, WarningThreshold> existingMap = new HashMap<>();
        for (WarningThreshold t : existing) {
            existingMap.put(t.getThresholdType(), t);
        }
        for (Map.Entry<String, Object> entry : thresholds.entrySet()) {
            String type = entry.getKey();
            Double value = Double.parseDouble(entry.getValue().toString());
            WarningThreshold existingRecord = existingMap.get(type);
            if (existingRecord != null) {
                existingRecord.setThresholdValue(value);
                warningThresholdMapper.updateById(existingRecord);
            } else {
                WarningThreshold newThreshold = new WarningThreshold();
                newThreshold.setUserId(userId);
                newThreshold.setThresholdType(type);
                newThreshold.setThresholdValue(value);
                warningThresholdMapper.insert(newThreshold);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "预警阈值保存成功");
        return result;
    }

    @Override
    public Map<String, Object> getPreventionPlan(Long userId) {
        Map<String, Object> plan = new HashMap<>();

        // 运动处方
        List<Map<String, String>> exercisePlan = new ArrayList<>();
        exercisePlan.add(createExerciseItem("快走/慢跑", "每周5次", "30分钟", "中等"));
        exercisePlan.add(createExerciseItem("力量训练", "每周2次", "20分钟", "低"));
        plan.put("exercisePlan", exercisePlan);

        // 饮食原则
        List<String> dietPrinciples = Arrays.asList(
            "控制总热量摄入，每日不超过1800千卡",
            "减少精制碳水，增加全谷物摄入",
            "增加蔬菜水果摄入，每日500g以上"
        );
        plan.put("dietPrinciples", dietPrinciples);

        // 用药指导
        List<Map<String, String>> medications = new ArrayList<>();
        medications.add(createMedicationItem("二甲双胍", "控制血糖", "500mg", "每日2次"));
        medications.add(createMedicationItem("阿托伐他汀", "调节血脂", "20mg", "每晚1次"));
        plan.put("medications", medications);

        return plan;
    }

    @Override
    public Map<String, Object> generatePreventionPlan(Map<String, Object> data) {
        Map<String, Object> plan = new HashMap<>();
        plan.put("success", true);
        plan.put("message", "预防方案生成成功");
        return plan;
    }

    @Override
    public List<Map<String, Object>> getHealthGoals(Long userId) {
        List<HealthGoal> records = healthGoalMapper.selectByUserId(userId);
        List<Map<String, Object>> goals = new ArrayList<>();
        for (HealthGoal record : records) {
            Map<String, Object> goal = new HashMap<>();
            goal.put("id", record.getId());
            goal.put("name", record.getGoalName());
            goal.put("target", record.getTargetValue() != null ? record.getTargetValue().toString() : "");
            goal.put("progress", record.getProgress() != null ? record.getProgress() : 0);
            goal.put("status", record.getStatus() != null ? record.getStatus() : 0);
            goal.put("currentValue", record.getCurrentValue());
            goals.add(goal);
        }
        return goals;
    }

    @Override
    public Map<String, Object> updateHealthGoal(Long userId, Long goalId, Map<String, Object> data) {
        HealthGoal goal = healthGoalMapper.selectById(goalId);
        if (goal != null && goal.getUserId().equals(userId)) {
            if (data.containsKey("goalName")) {
                goal.setGoalName(data.get("goalName").toString());
            }
            if (data.containsKey("targetValue")) {
                goal.setTargetValue(Double.parseDouble(data.get("targetValue").toString()));
            }
            if (data.containsKey("currentValue")) {
                goal.setCurrentValue(Double.parseDouble(data.get("currentValue").toString()));
            }
            if (data.containsKey("progress")) {
                goal.setProgress(Integer.parseInt(data.get("progress").toString()));
            }
            if (data.containsKey("status")) {
                goal.setStatus(Integer.parseInt(data.get("status").toString()));
            }
            healthGoalMapper.updateById(goal);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", goal != null && goal.getUserId().equals(userId));
        result.put("message", goal != null && goal.getUserId().equals(userId) ? "目标更新成功" : "目标不存在");
        return result;
    }

    @Override
    public String getHealthAdvice(Long userId) {
        return "根据您的健康数据，建议重点关注血糖控制和体重管理。近期血糖波动较大，建议减少精制碳水摄入，增加膳食纤维。同时，每周至少进行150分钟中等强度有氧运动，有助于改善胰岛素敏感性。";
    }

    // 辅助方法
    private Map<String, Object> createRisk(String disease, String level, int probability, List<String> factors) {
        Map<String, Object> risk = new HashMap<>();
        risk.put("disease", disease);
        risk.put("level", level);
        risk.put("probability", probability);
        risk.put("factors", factors);
        return risk;
    }

    private Map<String, Object> createDiseaseRisk(String disease, String level, int probability) {
        Map<String, Object> risk = new HashMap<>();
        risk.put("disease", disease);
        risk.put("level", level);
        risk.put("probability", probability);
        return risk;
    }

    private Map<String, Object> createFactor(String name, int impact, boolean controllable, String color, String description) {
        Map<String, Object> factor = new HashMap<>();
        factor.put("name", name);
        factor.put("impact", impact);
        factor.put("controllable", controllable);
        factor.put("color", color);
        factor.put("description", description);
        return factor;
    }

    private Map<String, String> createExerciseItem(String type, String frequency, String duration, String intensity) {
        Map<String, String> item = new HashMap<>();
        item.put("type", type);
        item.put("frequency", frequency);
        item.put("duration", duration);
        item.put("intensity", intensity);
        return item;
    }

    private Map<String, String> createMedicationItem(String name, String purpose, String dosage, String frequency) {
        Map<String, String> item = new HashMap<>();
        item.put("name", name);
        item.put("purpose", purpose);
        item.put("dosage", dosage);
        item.put("frequency", frequency);
        return item;
    }

}
