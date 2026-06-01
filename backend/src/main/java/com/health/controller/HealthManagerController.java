package com.health.controller;

import com.health.common.Result;
import com.health.entity.HealthProfile;
import com.health.entity.RiskAssessment;
import com.health.service.HealthManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health-manager")
@CrossOrigin
public class HealthManagerController {

    @Autowired
    private HealthManagerService healthManagerService;

    /**
     * 获取健康档案
     */
    @GetMapping("/profile/{userId}")
    public Result<HealthProfile> getHealthProfile(@PathVariable Long userId) {
        return Result.success(healthManagerService.getHealthProfile(userId));
    }

    /**
     * 更新健康档案
     */
    @PutMapping("/profile/{userId}")
    public Result<HealthProfile> updateHealthProfile(
            @PathVariable Long userId,
            @RequestBody HealthProfile profile) {
        profile.setUserId(userId);
        return Result.success(healthManagerService.updateHealthProfile(profile));
    }

    /**
     * 健康风险评估
     */
    @PostMapping("/risk-assessment")
    public Result<Map<String, Object>> assessRisk(@RequestBody Map<String, Object> assessmentData) {
        return Result.success(healthManagerService.assessRisk(assessmentData));
    }

    /**
     * 获取风险评估历史
     */
    @GetMapping("/risk-assessment/history/{userId}")
    public Result<List<RiskAssessment>> getAssessmentHistory(@PathVariable Long userId) {
        return Result.success(healthManagerService.getAssessmentHistory(userId));
    }

    /**
     * 生成饮食处方
     */
    @PostMapping("/diet-prescription")
    public Result<Map<String, Object>> generateDietPrescription(@RequestBody Map<String, Object> params) {
        return Result.success(healthManagerService.generateDietPrescription(params));
    }

    /**
     * 生成运动方案
     */
    @PostMapping("/exercise-plan")
    public Result<Map<String, Object>> generateExercisePlan(@RequestBody Map<String, Object> params) {
        return Result.success(healthManagerService.generateExercisePlan(params));
    }

    /**
     * 获取慢性病管理信息
     */
    @GetMapping("/disease-management/{userId}")
    public Result<Map<String, Object>> getDiseaseManagement(@PathVariable Long userId) {
        return Result.success(healthManagerService.getDiseaseManagement(userId));
    }

    /**
     * 添加监测记录
     */
    @PostMapping("/monitoring-record")
    public Result<Map<String, Object>> addMonitoringRecord(@RequestBody Map<String, Object> record) {
        return Result.success(healthManagerService.addMonitoringRecord(record));
    }

    /**
     * 获取用药提醒
     */
    @GetMapping("/medication-reminder/{userId}")
    public Result<List<Map<String, Object>>> getMedicationReminder(@PathVariable Long userId) {
        return Result.success(healthManagerService.getMedicationReminder(userId));
    }

    /**
     * 获取复查提醒
     */
    @GetMapping("/follow-up/{userId}")
    public Result<List<Map<String, Object>>> getFollowUpReminders(@PathVariable Long userId) {
        return Result.success(healthManagerService.getFollowUpReminders(userId));
    }

    /**
     * 获取AI健康建议
     */
    @GetMapping("/ai-advice/{userId}")
    public Result<List<Map<String, String>>> getAiAdvice(@PathVariable Long userId) {
        return Result.success(healthManagerService.getAiAdvice(userId));
    }
}
