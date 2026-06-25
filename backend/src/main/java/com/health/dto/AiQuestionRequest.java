package com.health.dto;

import lombok.Data;

@Data
public class AiQuestionRequest {
    /** 用户提问;为空表示生成综合健康方案。 */
    private String question;
    /** 结构化洞察的分析侧重:glucose/risk/diet/exercise/comprehensive;为空走自由问题。 */
    private String aspect;
}
