package com.health.service;

import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/** 调用 Python AI 服务的客户端。 */
@Component
@RequiredArgsConstructor
public class AiClient {

    private final WebClient aiWebClient;

    @SuppressWarnings("unchecked")
    public Map<String, Object> generateAdvice(Map<String, Object> body) {
        return aiWebClient.post()
                .uri("/api/agent/advice")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    /** SSE 流式生成,返回解码后的文本增量流。 */
    public Flux<String> streamAdvice(Map<String, Object> body) {
        return aiWebClient.post()
                .uri("/api/agent/advice/stream")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> assessRisk(Map<String, Object> body) {
        return aiWebClient.post()
                .uri("/api/risk/assess")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> healthPlan(Map<String, Object> body) {
        return aiWebClient.post()
                .uri("/api/health/plan")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> structuredHealthPlan(Map<String, Object> body) {
        return aiWebClient.post()
                .uri("/api/health/plan/structured")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> predict(Map<String, Object> body) {
        return aiWebClient.post()
                .uri("/api/analysis/predict")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> detectImage(byte[] bytes, String filename) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "image.png";
            }
        });
        return aiWebClient.post()
                .uri("/api/imaging/detect")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> analyzeReport(byte[] bytes, String filename, String contentType) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename != null ? filename : "report.png";
            }
        }).contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM);
        return aiWebClient.post()
                .uri("/api/reports/analyze")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> anomaly(Map<String, Object> body) {
        return aiWebClient.post()
                .uri("/api/analysis/anomaly")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
