package com.lzy.lostandfound.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzy.lostandfound.config.AiProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Component
public class OpenAiCompatibleClient implements AiModelClient {

    private final WebClient aiWebClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleClient(@Qualifier("aiWebClient") WebClient aiWebClient,
                                  AiProperties aiProperties,
                                  ObjectMapper objectMapper) {
        this.aiWebClient = aiWebClient;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String suggestByText(String systemPrompt, String userPrompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModelText());
        body.put("temperature", aiProperties.getTemperature());

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(buildTextMessage("system", systemPrompt));
        messages.add(buildTextMessage("user", userPrompt));
        body.put("messages", messages);

        return callAndParse(body);
    }

    @Override
    public String suggestByVision(String systemPrompt, String userPrompt, List<String> imageUrls) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiProperties.getModelVision());
        body.put("temperature", aiProperties.getTemperature());

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(buildTextMessage("system", systemPrompt));

        List<Map<String, Object>> visionContent = new ArrayList<>();
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("type", "text");
        textPart.put("text", userPrompt);
        visionContent.add(textPart);

        if (imageUrls != null) {
            for (String url : imageUrls) {
                Map<String, Object> imagePart = new HashMap<>();
                imagePart.put("type", "image_url");

                Map<String, Object> imageUrl = new HashMap<>();
                imageUrl.put("url", url);
                imagePart.put("image_url", imageUrl);
                visionContent.add(imagePart);
            }
        }

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", visionContent);
        messages.add(userMessage);

        body.put("messages", messages);
        return callAndParse(body);
    }

    private Map<String, Object> buildTextMessage(String role, String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String callAndParse(Map<String, Object> body) {
        if (!StringUtils.hasText(aiProperties.getApiKey())) {
            throw new RuntimeException("AI服务未配置，请联系管理员");
        }

        String raw;
        try {
            raw = aiWebClient.post()
                    .uri("/chat/completions")
                    .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + aiProperties.getApiKey()))
                    .bodyValue(body)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp ->
                            resp.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(respBody -> Mono.error(new RuntimeException(
                                            "AI模型服务调用失败，请稍后重试"
                                    )))
                    )
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(aiProperties.getTimeoutMs()))
                    .onErrorMap(TimeoutException.class, ex -> new RuntimeException("AI服务超时，请稍后重试"))
                    .block();
        } catch (RuntimeException ex) {
            if (containsTimeout(ex)) {
                throw new RuntimeException("AI服务超时，请稍后重试");
            }
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("AI模型服务调用失败，请稍后重试");
        }

        if (!StringUtils.hasText(raw)) {
            throw new RuntimeException("未生成有效描述，请重试");
        }

        try {
            JsonNode root = objectMapper.readTree(raw);
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            String text = extractContent(contentNode);
            if (!StringUtils.hasText(text)) {
                throw new RuntimeException("未生成有效描述，请重试");
            }
            return text.trim();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("AI响应解析失败，请稍后重试");
        }
    }

    private String extractContent(JsonNode contentNode) {
        if (contentNode == null || contentNode.isMissingNode() || contentNode.isNull()) {
            return null;
        }
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        if (contentNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode node : contentNode) {
                if (node == null || node.isNull()) {
                    continue;
                }
                if (node.isTextual()) {
                    sb.append(node.asText()).append('\n');
                    continue;
                }
                if (node.isObject()) {
                    JsonNode textNode = node.path("text");
                    if (textNode.isTextual()) {
                        sb.append(textNode.asText()).append('\n');
                    }
                }
            }
            String merged = sb.toString().trim();
            return StringUtils.hasText(merged) ? merged : null;
        }
        return null;
    }

    private boolean containsTimeout(Throwable ex) {
        Throwable cursor = ex;
        while (cursor != null) {
            if (cursor instanceof TimeoutException) {
                return true;
            }
            cursor = cursor.getCause();
        }
        return false;
    }
}
