package com.health.controller;

import com.health.common.Result;
import com.health.service.RiskWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk")
@CrossOrigin
public class RiskWarningController {

    @Autowired
    private RiskWarningService riskWarningService;

    /**
     * 获取风险评估
     */
    @GetMapping("/assessment/{userId}")
    public Result<Map<String, Object>> getRiskAssessment(@PathVariable Long userId) {
        return Result.success(riskWarningService.getRiskAssessment(userId));
    }

    /**
     * 提交风险评估
     */
    @PostMapping("/assessment")
    public Result<Map<String, Object>> submitAssessment(@RequestBody Map<String, Object> data) {
        return Result.success(riskWarningService.submitAssessment(data));
    }

    /**
     * 获取疾病风险详情
     */
    @GetMapping("/assessment/{userId}/{diseaseType}")
    public Result<Map<String, Object>> getRiskDetail(
            @PathVariable Long userId,
            @PathVariable String diseaseType) {
        return Result.success(riskWarningService.getRiskDetail(userId, diseaseType));
    }

    /**
     * 获取风险分级
     */
    @GetMapping("/levels/{userId}")
    public Result<Map<String, Object>> getRiskLevels(@PathVariable Long userId) {
        return Result.success(riskWarningService.getRiskLevels(userId));
    }

    /**
     * 获取风险因素
     */
    @GetMapping("/factors/{userId}")
    public Result<List<Map<String, Object>>> getRiskFactors(@PathVariable Long userId) {
        return Result.success(riskWarningService.getRiskFactors(userId));
    }

    /**
     * 获取预警列表
     */
    @GetMapping("/warnings")
    public Result<List<Map<String, Object>>> getWarnings(
            @RequestParam Long userId,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean read) {
        return Result.success(riskWarningService.getWarnings(userId, level, read));
    }

    /**
     * 标记预警已读
     */
    @PutMapping("/warnings/{id}/read")
    public Result<Map<String, Object>> markWarningRead(@PathVariable Long id) {
        return Result.success(riskWarningService.markWarningRead(id));
    }

    /**
     * 标记所有预警已读
     */
    @PutMapping("/warnings/read-all")
    public Result<Map<String, Object>> markAllWarningsRead(@RequestParam Long userId) {
        return Result.success(riskWarningService.markAllWarningsRead(userId));
    }

    /**
     * 忽略预警
     */
    @PutMapping("/warnings/{id}/dismiss")
    public Result<Map<String, Object>> dismissWarning(@PathVariable Long id) {
        return Result.success(riskWarningService.dismissWarning(id));
    }

    /**
     * 获取预警统计
     */
    @GetMapping("/warnings/stats/{userId}")
    public Result<Map<String, Object>> getWarningStats(@PathVariable Long userId) {
        return Result.success(riskWarningService.getWarningStats(userId));
    }

    /**
     * 获取预警阈值设置
     */
    @GetMapping("/thresholds/{userId}")
    public Result<Map<String, Object>> getWarningThresholds(@PathVariable Long userId) {
        return Result.success(riskWarningService.getWarningThresholds(userId));
    }

    /**
     * 更新预警阈值设置
     */
    @PutMapping("/thresholds/{userId}")
    public Result<Map<String, Object>> updateWarningThresholds(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> thresholds) {
        return Result.success(riskWarningService.updateWarningThresholds(userId, thresholds));
    }

    /**
     * 获取预防方案
     */
    @GetMapping("/prevention-plan/{userId}")
    public Result<Map<String, Object>> getPreventionPlan(@PathVariable Long userId) {
        return Result.success(riskWarningService.getPreventionPlan(userId));
    }

    /**
     * 生成预防方案
     */
    @PostMapping("/prevention-plan/generate")
    public Result<Map<String, Object>> generatePreventionPlan(@RequestBody Map<String, Object> data) {
        return Result.success(riskWarningService.generatePreventionPlan(data));
    }

    /**
     * 获取健康目标
     */
    @GetMapping("/goals/{userId}")
    public Result<List<Map<String, Object>>> getHealthGoals(@PathVariable Long userId) {
        return Result.success(riskWarningService.getHealthGoals(userId));
    }

    /**
     * 更新健康目标
     */
    @PutMapping("/goals/{userId}/{goalId}")
    public Result<Map<String, Object>> updateHealthGoal(
            @PathVariable Long userId,
            @PathVariable Long goalId,
            @RequestBody Map<String, Object> data) {
        return Result.success(riskWarningService.updateHealthGoal(userId, goalId, data));
    }

    /**
     * 获取AI健康建议
     */
    @GetMapping("/advice/{userId}")
    public Result<String> getHealthAdvice(@PathVariable Long userId) {
        return Result.success(riskWarningService.getHealthAdvice(userId));
    }
}
