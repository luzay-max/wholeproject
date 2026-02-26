package com.lzy.lostandfound.service;

import com.lzy.lostandfound.config.RiskControlProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskControlService {

    private static final DateTimeFormatter MINUTE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final StringRedisTemplate redisTemplate;
    private final RiskControlProperties riskControlProperties;

    public String checkPublishRate(String userId) {
        return checkRateLimit("publish", userId, riskControlProperties.getPublishPerMinute(), "发布过于频繁，请稍后再试");
    }

    public String checkCommentRate(String userId) {
        return checkRateLimit("comment", userId, riskControlProperties.getCommentPerMinute(), "评论过于频繁，请稍后再试");
    }

    public String checkReportRate(String userId) {
        return checkRateLimit("report", userId, riskControlProperties.getReportPerMinute(), "举报提交过于频繁，请稍后再试");
    }

    public String checkSensitiveContent(String value, String fieldName) {
        if (!riskControlProperties.isEnabled() || !riskControlProperties.isSensitiveEnabled()) {
            return null;
        }
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalizedValue = normalize(value);
        for (String word : riskControlProperties.getSensitiveWords()) {
            if (!StringUtils.hasText(word)) {
                continue;
            }
            String normalizedWord = normalize(word);
            if (!StringUtils.hasText(normalizedWord)) {
                continue;
            }
            if (normalizedValue.contains(normalizedWord)) {
                return fieldName + "包含敏感内容，请修改后提交";
            }
        }
        return null;
    }

    private String checkRateLimit(String scene, String userId, int maxPerMinute, String exceedMessage) {
        if (!riskControlProperties.isEnabled() || !riskControlProperties.isRateLimitEnabled()) {
            return null;
        }
        if (!StringUtils.hasText(userId) || maxPerMinute <= 0) {
            return null;
        }
        String minute = LocalDateTime.now().format(MINUTE_FORMATTER);
        String key = "risk:rate:" + scene + ":" + userId + ":" + minute;
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(70));
            }
            if (count != null && count > maxPerMinute) {
                return exceedMessage;
            }
        } catch (Exception ex) {
            log.warn("风控限流检查失败，scene={}, userId={}", scene, userId, ex);
        }
        return null;
    }

    private String normalize(String value) {
        return value
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", "");
    }
}

