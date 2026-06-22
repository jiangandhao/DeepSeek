package com.health.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.health.common.Result;
import com.health.dto.AiQuestionRequest;
import com.health.entity.AiAdvice;
import com.health.entity.Alert;
import com.health.security.UserContext;
import com.health.service.AiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "风险预警 / 数智健管师")
@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {

    private final AiService aiService;

    @Operation(summary = "疾病风险评估(中/高风险自动落库预警)")
    @PostMapping("/assess")
    public Result<Map<String, Object>> assess() {
        return Result.ok(aiService.assessRisk(UserContext.currentUserId()));
    }

    @Operation(summary = "AI 数智健管师:风险解读 + 个性化处方")
    @PostMapping("/health-plan")
    public Result<AiAdvice> healthPlan(@RequestBody(required = false) AiQuestionRequest req) {
        String q = req != null ? req.getQuestion() : null;
        return Result.ok(aiService.healthPlan(UserContext.currentUserId(), q));
    }

    @Operation(summary = "结构化饮食方案、营养目标与七日运动计划")
    @PostMapping("/structured-plan")
    public Result<Map<String, Object>> structuredPlan() {
        return Result.ok(aiService.structuredHealthPlan(UserContext.currentUserId()));
    }

    @Operation(summary = "保存用户确认的结构化综合方案")
    @PostMapping("/plans")
    public Result<AiAdvice> savePlan(@RequestBody Map<String, Object> plan) {
        return Result.ok(aiService.saveStructuredPlan(UserContext.currentUserId(), plan));
    }

    @Operation(summary = "已保存的综合方案历史")
    @GetMapping("/plans")
    public Result<List<AiAdvice>> planHistory() {
        return Result.ok(aiService.planHistory(UserContext.currentUserId()));
    }

    @Operation(summary = "预警列表")
    @GetMapping("/alerts")
    public Result<List<Alert>> alerts() {
        return Result.ok(aiService.alerts(UserContext.currentUserId()));
    }

    @Operation(summary = "标记预警已读")
    @PutMapping("/alerts/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        aiService.markAlertRead(UserContext.currentUserId(), id);
        return Result.ok();
    }
}
