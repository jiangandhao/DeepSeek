package com.health.service;

import com.health.entity.HealthProfile;
import com.health.entity.RiskAssessment;

import java.util.List;
import java.util.Map;

public interface HealthManagerService {

    /**
     * 获取健康档案
     */
    HealthProfile getHealthProfile(Long userId);

    /**
     * 更新健康档案
     */
    HealthProfile updateHealthProfile(HealthProfile profile);

    /**
     * 健康风险评估
     */
    Map<String, Object> assessRisk(Map<String, Object> assessmentData);

    /**
     * 获取风险评估历史
     */
    List<RiskAssessment> getAssessmentHistory(Long userId);

    /**
     * 生成饮食处方
     */
    Map<String, Object> generateDietPrescription(Map<String, Object> params);

    /**
     * 生成运动方案
     */
    Map<String, Object> generateExercisePlan(Map<String, Object> params);

    /**
     * 获取慢性病管理信息
     */
    Map<String, Object> getDiseaseManagement(Long userId);

    /**
     * 添加监测记录
     */
    Map<String, Object> addMonitoringRecord(Map<String, Object> record);

    /**
     * 获取用药提醒
     */
    List<Map<String, Object>> getMedicationReminder(Long userId);

    /**
     * 获取复查提醒
     */
    List<Map<String, Object>> getFollowUpReminders(Long userId);

    /**
     * 获取AI健康建议
     */
    List<Map<String, String>> getAiAdvice(Long userId);
}
