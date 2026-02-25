package com.lzy.lostandfound.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    private boolean enabled = false;
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey;
    private String modelText = "gpt-4o-mini";
    private String modelVision = "gpt-4o-mini";
    private long timeoutMs = 15000L;
    private int maxImages = 3;
    private int rateLimitPerMinute = 10;
    private long cacheTtlSeconds = 600L;
    private double temperature = 0.2D;
}

