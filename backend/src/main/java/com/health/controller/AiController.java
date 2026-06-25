package com.health.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.health.common.Result;
import com.health.dto.AiQuestionRequest;
import com.health.entity.AiAdvice;
import com.health.security.UserContext;
import com.health.service.AiService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Tag(name = "AI 血糖智能体")
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "生成健康方案 / 回答健康问题(非流式,落库)")
    @PostMapping("/advice")
    public Result<AiAdvice> advice(@RequestBody(required = false) AiQuestionRequest req) {
        String question = req != null ? req.getQuestion() : null;
        return Result.ok(aiService.advice(UserContext.currentUserId(), question));
    }

    @Operation(summary = "历史方案")
    @GetMapping("/advice")
    public Result<List<AiAdvice>> history() {
        return Result.ok(aiService.adviceHistory(UserContext.currentUserId()));
    }

    @Operation(summary = "结构化洞察(返回可视化卡片 JSON)")
    @PostMapping("/insight")
    public Result<Map<String, Object>> insight(@RequestBody(required = false) AiQuestionRequest req) {
        String aspect = req != null ? req.getAspect() : null;
        String question = req != null ? req.getQuestion() : null;
        return Result.ok(aiService.insight(UserContext.currentUserId(), aspect, question));
    }

    @Operation(summary = "流式洞察(SSE,逐字输出 Markdown)")
    @PostMapping(value = "/insight/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> insightStream(@RequestBody(required = false) AiQuestionRequest req) {
        String aspect = req != null ? req.getAspect() : null;
        String question = req != null ? req.getQuestion() : null;
        return aiService.streamInsight(UserContext.currentUserId(), aspect, question);
    }

    @Operation(summary = "流式对话(SSE)")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody(required = false) AiQuestionRequest req) {
        String question = req != null ? req.getQuestion() : null;
        return aiService.streamAdvice(UserContext.currentUserId(), question);
    }

    @Operation(summary = "血糖趋势预测")
    @PostMapping("/predict")
    public Result<Map<String, Object>> predict(@RequestParam(defaultValue = "6") int horizon) {
        return Result.ok(aiService.predict(UserContext.currentUserId(), horizon));
    }

    @Operation(summary = "血糖异常检测")
    @GetMapping("/anomaly")
    public Result<Map<String, Object>> anomaly() {
        return Result.ok(aiService.anomaly(UserContext.currentUserId()));
    }
}
