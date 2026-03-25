package com.lzy.lostandfound.service;

import com.lzy.lostandfound.config.RiskControlProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskControlServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private RiskControlProperties riskControlProperties;
    private RiskControlService riskControlService;

    @BeforeEach
    void setUp() {
        riskControlProperties = new RiskControlProperties();
        riskControlProperties.setEnabled(true);
        riskControlProperties.setSensitiveEnabled(true);
        riskControlProperties.setRateLimitEnabled(true);
        riskControlProperties.setPublishPerMinute(6);
        riskControlProperties.setCommentPerMinute(20);
        riskControlProperties.setReportPerMinute(10);
        riskControlProperties.setSensitiveWords(List.of("诈骗", "赌博"));
        riskControlService = new RiskControlService(redisTemplate, riskControlProperties);
    }

    @Test
    void checkSensitiveContentShouldRejectSensitiveWord() {
        String result = riskControlService.checkSensitiveContent("这是诈骗链接", "举报说明");
        assertEquals("举报说明包含敏感内容，请修改后提交", result);
    }

    @Test
    void checkSensitiveContentShouldPassWhenClean() {
        String result = riskControlService.checkSensitiveContent("在图书馆门口捡到校园卡", "物品描述");
        assertNull(result);
    }

    @Test
    void checkPublishRateShouldReturnErrorWhenExceeded() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(startsWith("risk:rate:publish:user-1:"))).thenReturn(7L);

        String result = riskControlService.checkPublishRate("user-1");

        assertEquals("发布过于频繁，请稍后再试", result);
    }

    @Test
    void checkPublishRateShouldPassWithinLimit() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(startsWith("risk:rate:publish:user-1:"))).thenReturn(1L);

        String result = riskControlService.checkPublishRate("user-1");

        assertNull(result);
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }
}
