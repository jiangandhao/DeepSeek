package com.health.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.common.BizException;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.health.entity.AiAdvice;
import com.health.entity.Alert;
import com.health.entity.DietRecord;
import com.health.entity.ExerciseRecord;
import com.health.entity.GlucoseRecord;
import com.health.entity.HealthProfile;
import com.health.entity.User;
import com.health.mapper.AiAdviceMapper;
import com.health.mapper.AlertMapper;
import com.health.mapper.UserMapper;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final GlucoseService glucoseService;
    private final DietService dietService;
    private final ExerciseService exerciseService;
    private final HealthProfileService healthProfileService;
    private final DeviceService deviceService;
    private final UserMapper userMapper;
    private final AiAdviceMapper aiAdviceMapper;
    private final AlertMapper alertMapper;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    private List<Map<String, Object>> glucosePoints(List<GlucoseRecord> records) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (GlucoseRecord r : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("value_mmol", r.getValueMmol());
            m.put("period", r.getPeriod());
            m.put("measured_at", r.getMeasuredAt().format(FMT));
            list.add(m);
        }
        return list;
    }

    private Map<String, Object> buildProfile(Long userId) {
        Map<String, Object> profile = new HashMap<>();
        User u = userMapper.selectById(userId);
        if (u != null) {
            if (u.getGender() != null) {
                profile.put("性别", u.getGender() == 1 ? "男" : u.getGender() == 2 ? "女" : "未知");
            }
            if (u.getBirthDate() != null) {
                int age = java.time.Period.between(u.getBirthDate(), java.time.LocalDate.now()).getYears();
                profile.put("age", age);
            }
        }
        // 健康档案(字段名与 AI 服务风险评分器对齐)
        HealthProfile hp = healthProfileService.get(userId);
        if (hp != null) {
            if (hp.getHeightCm() != null) profile.put("height_cm", hp.getHeightCm());
            if (hp.getWeightKg() != null) profile.put("weight_kg", hp.getWeightKg());
            if (hp.getFamilyHistory() != null) profile.put("family_history", hp.getFamilyHistory());
            if (hp.getChronic() != null) profile.put("chronic", hp.getChronic());
            if (hp.getDiabetesType() != null) {
                String[] t = {"无", "1型", "2型", "妊娠"};
                int idx = hp.getDiabetesType();
                profile.put("糖尿病类型", idx >= 0 && idx < t.length ? t[idx] : "未知");
            }
        }
        return profile;
    }

    private Map<String, Object> buildAdviceBody(Long userId, String question, boolean stream) {
        List<GlucoseRecord> glucose = glucoseService.recent(userId, 30);
        List<DietRecord> diet = dietService.recent(userId, 10);
        List<ExerciseRecord> exercise = exerciseService.recent(userId, 10);

        Map<String, Object> body = new HashMap<>();
        body.put("profile", buildProfile(userId));
        body.put("glucose", glucosePoints(glucose));
        body.put("diet", diet);
        body.put("exercise", exercise);
        body.put("devices", deviceService.list(userId));
        body.put("question", question);
        body.put("stream", stream);
        return body;
    }

    /** 生成并保存健康方案。 */
    public AiAdvice advice(Long userId, String question) {
        Map<String, Object> body = buildAdviceBody(userId, question, false);
        Map<String, Object> result = aiClient.generateAdvice(body);

        AiAdvice advice = new AiAdvice();
        advice.setUserId(userId);
        advice.setType(question == null || question.isBlank() ? "COMPREHENSIVE" : "QA");
        advice.setContent(result != null ? String.valueOf(result.get("content")) : "");
        advice.setContext(result != null ? String.valueOf(result.get("context")) : null);
        aiAdviceMapper.insert(advice);
        return advice;
    }

    /** 结构化洞察:让 AI 针对某一方面返回可渲染为卡片的 JSON(不落库)。 */
    public Map<String, Object> insight(Long userId, String aspect, String question) {
        Map<String, Object> body = buildAdviceBody(userId, question, false);
        body.put("aspect", aspect);
        return aiClient.insight(body);
    }

    /** 流式洞察:逐字返回 Markdown 分析(代理 AI 服务 SSE)。 */
    public Flux<String> streamInsight(Long userId, String aspect, String question) {
        Map<String, Object> body = buildAdviceBody(userId, question, true);
        body.put("aspect", aspect);
        return aiClient.streamInsight(body);
    }

    /** 流式生成(代理 AI 服务 SSE)。 */
    public Flux<String> streamAdvice(Long userId, String question) {
        Map<String, Object> body = buildAdviceBody(userId, question, true);
        return aiClient.streamAdvice(body);
    }

    public List<AiAdvice> adviceHistory(Long userId) {
        return aiAdviceMapper.selectList(Wrappers.<AiAdvice>lambdaQuery()
                .eq(AiAdvice::getUserId, userId)
                .orderByDesc(AiAdvice::getCreatedAt));
    }

    /** 疾病风险评估;中/高风险落库 alert 表。 */
    public Map<String, Object> assessRisk(Long userId) {
        List<GlucoseRecord> glucose = glucoseService.recent(userId, 50);
        Map<String, Object> body = new HashMap<>();
        body.put("profile", buildProfile(userId));
        body.put("glucose", glucosePoints(glucose));
        Map<String, Object> risk = aiClient.assessRisk(body);

        if (risk != null) {
            String level = String.valueOf(risk.get("level"));
            if ("MEDIUM".equals(level) || "HIGH".equals(level)) {
                Alert alert = new Alert();
                alert.setUserId(userId);
                alert.setCategory("RISK");
                alert.setLevel(level);
                alert.setMessage(String.valueOf(risk.get("message")));
                alert.setIsRead(0);
                alertMapper.insert(alert);
            }
        }
        return risk;
    }

    /** AI 数智健管师:风险解读 + 个性化处方,落库 ai_advice。 */
    public AiAdvice healthPlan(Long userId, String question) {
        Map<String, Object> body = buildAdviceBody(userId, question, false);
        Map<String, Object> result = aiClient.healthPlan(body);

        AiAdvice advice = new AiAdvice();
        advice.setUserId(userId);
        advice.setType("HEALTH_PLAN");
        advice.setContent(result != null ? String.valueOf(result.get("content")) : "");
        advice.setContext(result != null ? String.valueOf(result.get("context")) : null);
        aiAdviceMapper.insert(advice);
        return advice;
    }

    /** 返回可直接渲染的饮食、营养目标与七日运动计划。 */
    public Map<String, Object> structuredHealthPlan(Long userId) {
        Map<String, Object> body = buildAdviceBody(userId, null, false);
        return aiClient.structuredHealthPlan(body);
    }

    /** 用户确认后显式保存当前结构化方案。 */
    public AiAdvice saveStructuredPlan(Long userId, Map<String, Object> plan) {
        if (plan == null || !plan.containsKey("meals") || !plan.containsKey("exercise_week")) {
            throw new BizException("方案内容不完整，请重新生成");
        }
        AiAdvice advice = new AiAdvice();
        advice.setUserId(userId);
        advice.setType("SAVED_PLAN");
        try {
            advice.setContent(objectMapper.writeValueAsString(plan));
        } catch (JsonProcessingException e) {
            throw new BizException("结构化健康方案保存失败");
        }
        advice.setContext("用户确认保存的饮食与运动综合方案");
        aiAdviceMapper.insert(advice);
        return advice;
    }

    public List<AiAdvice> planHistory(Long userId) {
        return aiAdviceMapper.selectList(Wrappers.<AiAdvice>lambdaQuery()
                .eq(AiAdvice::getUserId, userId)
                .eq(AiAdvice::getType, "SAVED_PLAN")
                .orderByDesc(AiAdvice::getCreatedAt));
    }

    public List<Alert> alerts(Long userId) {
        return alertMapper.selectList(Wrappers.<Alert>lambdaQuery()
                .eq(Alert::getUserId, userId)
                .orderByDesc(Alert::getCreatedAt));
    }

    public void markAlertRead(Long userId, Long id) {
        Alert a = alertMapper.selectById(id);
        if (a != null && a.getUserId().equals(userId)) {
            a.setIsRead(1);
            alertMapper.updateById(a);
        }
    }

    public Map<String, Object> predict(Long userId, int horizon) {
        List<GlucoseRecord> glucose = glucoseService.recent(userId, 50);
        Map<String, Object> body = new HashMap<>();
        body.put("history", glucosePoints(glucose));
        body.put("horizon", horizon);
        return aiClient.predict(body);
    }

    public Map<String, Object> anomaly(Long userId) {
        List<GlucoseRecord> glucose = glucoseService.recent(userId, 50);
        Map<String, Object> body = new HashMap<>();
        body.put("history", glucosePoints(glucose));
        return aiClient.anomaly(body);
    }
}
