package com.health.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** 调用 Python AI 服务的 WebClient。 */
@Configuration
public class WebClientConfig {

    /** 内部服务共享密钥头名称(须与 AI 服务一致)。 */
    public static final String INTERNAL_KEY_HEADER = "X-Internal-Key";

    @Bean
    public WebClient aiWebClient(
            @Value("${app.ai-service.url}") String baseUrl,
            @Value("${app.ai-service.internal-key:}") String internalKey) {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(c -> c.defaultCodecs().maxInMemorySize(8 * 1024 * 1024));
        // 仅在配置了密钥时附加,避免本地无密钥开发时传空头
        if (internalKey != null && !internalKey.isBlank()) {
            builder.defaultHeader(INTERNAL_KEY_HEADER, internalKey);
        }
        return builder.build();
    }
}
