package com.lzy.lostandfound.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzy.lostandfound.ai.AiModelClient;
import com.lzy.lostandfound.config.AiProperties;
import com.lzy.lostandfound.dto.AiDescriptionSuggestRequest;
import com.lzy.lostandfound.dto.AiDescriptionSuggestResponse;
import com.lzy.lostandfound.service.IAiDescriptionService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class AiDescriptionServiceImpl implements IAiDescriptionService {

    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final String DEFAULT_STYLE = "objective_concise";
    private static final String SOURCE_TEXT = "text";
    private static final String SOURCE_VISION = "vision";
    private static final String SOURCE_MIXED = "mixed";
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)1\\d{10}(?!\\d)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final DateTimeFormatter LIMIT_MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final AiModelClient aiModelClient;
    private final AiProperties aiProperties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public AiDescriptionServiceImpl(AiModelClient aiModelClient,
                                    AiProperties aiProperties,
                                    StringRedisTemplate redisTemplate,
                                    ObjectMapper objectMapper) {
        this.aiModelClient = aiModelClient;
        this.aiProperties = aiProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public AiDescriptionSuggestResponse suggestDescription(String userId, AiDescriptionSuggestRequest request) {
        if (!aiProperties.isEnabled()) {
            throw new IllegalStateException("AI功能未开启");
        }
        if (!StringUtils.hasText(userId)) {
            throw new IllegalStateException("未登录或登录信息失效");
        }
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }

        NormalizedInput input = normalizeRequest(request);
        applyRateLimit(userId);

        String cacheKey = buildCacheKey(userId, input);
        AiDescriptionSuggestResponse cached = getCachedResponse(cacheKey);
        if (cached != null) {
            return cached;
        }

        String systemPrompt = buildSystemPrompt();
        String basePrompt = buildUserPrompt(input, false);

        String suggestion;
        String sourceMode;
        String notice = null;

        if (!input.imageUrls().isEmpty()) {
            try {
                String visionPrompt = buildUserPrompt(input, true);
                suggestion = aiModelClient.suggestByVision(systemPrompt, visionPrompt, input.imageUrls());
                sourceMode = StringUtils.hasText(input.currentDescription()) ? SOURCE_MIXED : SOURCE_VISION;
            } catch (RuntimeException ex) {
                suggestion = suggestByTextSafely(systemPrompt, basePrompt);
                sourceMode = SOURCE_TEXT;
                notice = "图片增强失败，已切换文本补全";
            }
        } else {
            suggestion = suggestByTextSafely(systemPrompt, basePrompt);
            sourceMode = SOURCE_TEXT;
        }

        SanitizedText sanitized = sanitizeSuggestion(suggestion);
        if (!StringUtils.hasText(sanitized.text)) {
            throw new RuntimeException("未生成有效描述，请重试");
        }
        if (!CHINESE_PATTERN.matcher(sanitized.text).find()) {
            throw new RuntimeException("AI未生成中文描述，请重试");
        }

        AiDescriptionSuggestResponse response = new AiDescriptionSuggestResponse(
                sanitized.text,
                sourceMode,
                notice,
                sanitized.truncated
        );
        cacheResponse(cacheKey, response);
        return response;
    }

    private void applyRateLimit(String userId) {
        String minutePart = LocalDateTime.now(ZoneId.of("Asia/Shanghai")).format(LIMIT_MINUTE_FORMATTER);
        String key = "ai:suggest:limit:" + userId + ":" + minutePart;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(70));
        }
        if (count != null && count > aiProperties.getRateLimitPerMinute()) {
            throw new IllegalStateException("请求过于频繁，请稍后再试");
        }
    }

    private AiDescriptionSuggestResponse getCachedResponse(String cacheKey) {
        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (!StringUtils.hasText(cached)) {
                return null;
            }
            return objectMapper.readValue(cached, AiDescriptionSuggestResponse.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void cacheResponse(String cacheKey, AiDescriptionSuggestResponse response) {
        try {
            String json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(cacheKey, json, Duration.ofSeconds(aiProperties.getCacheTtlSeconds()));
        } catch (Exception ignored) {
            // 缓存写入失败不影响主流程
        }
    }

    private String buildCacheKey(String userId, NormalizedInput input) {
        String rawPayload = input.itemKind() + "|" + input.name() + "|" + input.type() + "|" + input.location() + "|"
                + input.timeValue() + "|" + input.style() + "|" + input.currentDescription() + "|" + String.join(",", input.imageUrls());
        return "ai:suggest:cache:" + userId + ":" + sha256(rawPayload);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new RuntimeException("缓存键计算失败");
        }
    }

    private NormalizedInput normalizeRequest(AiDescriptionSuggestRequest request) {
        String itemKind = lower(trimToEmpty(request.getItemKind()));
        String name = maskSensitive(trimToEmpty(request.getName()));
        String type = maskSensitive(trimToEmpty(request.getType()));
        String location = maskSensitive(trimToEmpty(request.getLocation()));
        String timeValue = maskSensitive(trimToEmpty(request.getTimeValue()));
        String currentDescription = maskSensitive(trimToEmpty(request.getCurrentDescription()));

        String style = lower(trimToEmpty(request.getStyle()));
        if (!StringUtils.hasText(style)) {
            style = DEFAULT_STYLE;
        }

        List<String> imageUrls = new ArrayList<>();
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            LinkedHashSet<String> dedup = new LinkedHashSet<>();
            for (String url : request.getImageUrls()) {
                String trimmed = trimToEmpty(url);
                if (!StringUtils.hasText(trimmed)) {
                    continue;
                }
                String lower = trimmed.toLowerCase(Locale.ROOT);
                if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
                    continue;
                }
                dedup.add(trimmed);
                if (dedup.size() >= aiProperties.getMaxImages()) {
                    break;
                }
            }
            imageUrls.addAll(dedup);
        }

        return new NormalizedInput(itemKind, name, type, location, timeValue, imageUrls, currentDescription, style);
    }

    private String buildSystemPrompt() {
        return "你是校园失物招领信息助手。请根据给定信息输出一段中文“客观简洁”描述。"
                + "禁止输出联系方式、价格推断、主观判断和编造内容。"
                + "如果信息不确定，请省略不确定内容。输出只允许一段正文，不要列表。";
    }

    private String buildUserPrompt(NormalizedInput input, boolean withVision) {
        String kindCn = "lost".equalsIgnoreCase(input.itemKind()) ? "失物" : "招领";
        StringBuilder sb = new StringBuilder();
        sb.append("任务：为").append(kindCn).append("信息生成描述。\n")
                .append("物品名称：").append(input.name()).append("\n")
                .append("物品类型：").append(input.type()).append("\n")
                .append("地点：").append(input.location()).append("\n");
        if (StringUtils.hasText(input.timeValue())) {
            sb.append("时间：").append(input.timeValue()).append("\n");
        }
        if (StringUtils.hasText(input.currentDescription())) {
            sb.append("当前描述（可参考，不要重复）：").append(input.currentDescription()).append("\n");
        }

        sb.append("输出要求：")
                .append("1）40-180字；")
                .append("2）突出可识别特征（颜色、品牌、型号、材质、磨损、特殊标记、配件）；")
                .append("3）仅中文；")
                .append("4）不得包含联系方式和价格。");

        if (withVision) {
            sb.append("请结合图片可见信息补充细节，不确定就不要写。");
        }
        return sb.toString();
    }

    private SanitizedText sanitizeSuggestion(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new RuntimeException("未生成有效描述，请重试");
        }

        String text = raw.trim();
        text = text.replace("```json", "```");
        text = text.replace("```text", "```");
        text = text.replace("```", "");
        text = text.replaceAll("^(描述|建议描述|AI描述)[:：]\\s*", "");
        text = text.replaceAll("[\\t\\x0B\\f\\r]+", " ");
        text = text.replaceAll("\\n{3,}", "\n\n");
        text = text.trim();

        if (!StringUtils.hasText(text)) {
            throw new RuntimeException("未生成有效描述，请重试");
        }

        boolean truncated = false;
        if (text.length() > MAX_DESCRIPTION_LENGTH) {
            text = text.substring(0, MAX_DESCRIPTION_LENGTH);
            truncated = true;
        }
        return new SanitizedText(text, truncated);
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private String suggestByTextSafely(String systemPrompt, String userPrompt) {
        String result = aiModelClient.suggestByText(systemPrompt, userPrompt);
        if (!StringUtils.hasText(result)) {
            throw new RuntimeException("未生成有效描述，请重试");
        }
        return result;
    }

    private String maskSensitive(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String result = EMAIL_PATTERN.matcher(value).replaceAll("[已脱敏邮箱]");
        return PHONE_PATTERN.matcher(result).replaceAll("[已脱敏手机号]");
    }

    private record NormalizedInput(
            String itemKind,
            String name,
            String type,
            String location,
            String timeValue,
            List<String> imageUrls,
            String currentDescription,
            String style
    ) {}

    private static class SanitizedText {
        private final String text;
        private final boolean truncated;

        private SanitizedText(String text, boolean truncated) {
            this.text = text;
            this.truncated = truncated;
        }
    }
}
