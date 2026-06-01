package com.health.service;

import java.util.List;
import java.util.Map;

public interface RiskWarningService {

    /**
     * 获取风险评估
     */
    Map<String, Object> getRiskAssessment(Long userId);

    /**
     * 提交风险评估
     */
    Map<String, Object> submitAssessment(Map<String, Object> data);

    /**
     * 获取疾病风险详情
     */
    Map<String, Object> getRiskDetail(Long userId, String diseaseType);

    /**
     * 获取风险分级
     */
    Map<String, Object> getRiskLevels(Long userId);

    /**
     * 获取风险因素
     */
    List<Map<String, Object>> getRiskFactors(Long userId);

    /**
     * 获取预警列表
     */
    List<Map<String, Object>> getWarnings(Long userId, String level, Boolean read);

    /**
     * 标记预警已读
     */
    Map<String, Object> markWarningRead(Long id);

    /**
     * 标记所有预警已读
     */
    Map<String, Object> markAllWarningsRead(Long userId);

    /**
     * 忽略预警
     */
    Map<String, Object> dismissWarning(Long id);

    /**
     * 获取预警统计
     */
    Map<String, Object> getWarningStats(Long userId);

    /**
     * 获取预警阈值设置
     */
    Map<String, Object> getWarningThresholds(Long userId);

    /**
     * 更新预警阈值设置
     */
    Map<String, Object> updateWarningThresholds(Long userId, Map<String, Object> thresholds);

    /**
     * 获取预防方案
     */
    Map<String, Object> getPreventionPlan(Long userId);

    /**
     * 生成预防方案
     */
    Map<String, Object> generatePreventionPlan(Map<String, Object> data);

    /**
     * 获取健康目标
     */
    List<Map<String, Object>> getHealthGoals(Long userId);

    /**
     * 更新健康目标
     */
    Map<String, Object> updateHealthGoal(Long userId, Long goalId, Map<String, Object> data);

    /**
     * 获取AI健康建议
     */
    String getHealthAdvice(Long userId);
}
