package com.health.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.common.BizException;
import com.health.entity.HealthReport;
import com.health.mapper.HealthReportMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HealthReportService {
    private final HealthReportMapper mapper;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    public HealthReport analyze(Long userId, byte[] content, String filename, String contentType) {
        Map<String, Object> result = aiClient.analyzeReport(content, filename, contentType);
        HealthReport report = new HealthReport();
        report.setUserId(userId);
        report.setFilename(filename);
        report.setReportType(value(result, "report_type", "GENERAL_EXAM_REPORT"));
        report.setExtractionMethod(value(result, "extraction_method", "UNKNOWN"));
        report.setRiskLevel(value(result, "risk_level", "LOW"));
        report.setSummary(value(result, "summary", ""));
        Object count = result.get("abnormal_count");
        report.setAbnormalCount(count instanceof Number ? ((Number) count).intValue() : 0);
        try {
            report.setStructuredJson(objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new BizException("报告结构化结果保存失败");
        }
        mapper.insert(report);
        return report;
    }

    public List<HealthReport> list(Long userId) {
        return mapper.selectList(Wrappers.<HealthReport>lambdaQuery()
                .select(HealthReport::getId, HealthReport::getFilename, HealthReport::getReportType,
                        HealthReport::getExtractionMethod, HealthReport::getRiskLevel,
                        HealthReport::getSummary, HealthReport::getAbnormalCount, HealthReport::getCreatedAt)
                .eq(HealthReport::getUserId, userId)
                .orderByDesc(HealthReport::getCreatedAt));
    }

    public HealthReport get(Long userId, Long id) {
        HealthReport report = mapper.selectById(id);
        if (report == null || !report.getUserId().equals(userId)) {
            throw new BizException("报告不存在");
        }
        return report;
    }

    public void delete(Long userId, Long id) {
        get(userId, id);
        mapper.deleteById(id);
    }

    private String value(Map<String, Object> map, String key, String fallback) {
        Object value = map == null ? null : map.get(key);
        return value == null ? fallback : String.valueOf(value);
    }
}
