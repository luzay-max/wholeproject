package com.lzy.lostandfound.controller;

import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.dto.AiDescriptionSuggestRequest;
import com.lzy.lostandfound.dto.AiDescriptionSuggestResponse;
import com.lzy.lostandfound.service.IAiDescriptionService;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/ai")
public class AiAssistController {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)1\\d{10}(?!\\d)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");

    @Autowired
    private IAiDescriptionService aiDescriptionService;

    @PostMapping("/description/suggest")
    @Log("AI_SUGGEST_DESCRIPTION")
    public Result<AiDescriptionSuggestResponse> suggestDescription(@Valid @RequestBody AiDescriptionSuggestRequest request) {
        Map<String, Object> userMap = ThreadLocalUtil.get();
        if (userMap == null || userMap.get("id") == null) {
            return Result.notLogin("请先登录");
        }

        sanitizeRequestInPlace(request);
        String userId = String.valueOf(userMap.get("id"));
        return Result.success(aiDescriptionService.suggestDescription(userId, request));
    }

    private void sanitizeRequestInPlace(AiDescriptionSuggestRequest request) {
        request.setName(maskSensitive(request.getName()));
        request.setType(maskSensitive(request.getType()));
        request.setLocation(maskSensitive(request.getLocation()));
        request.setTimeValue(maskSensitive(request.getTimeValue()));
        request.setCurrentDescription(maskSensitive(request.getCurrentDescription()));
        request.setStyle(trimToNull(request.getStyle()));
        request.setItemKind(trimToNull(request.getItemKind()));

        if (request.getImageUrls() == null) {
            return;
        }
        List<String> cleaned = new ArrayList<>();
        for (String url : request.getImageUrls()) {
            String trimmed = trimToNull(url);
            if (trimmed != null) {
                cleaned.add(trimmed);
            }
        }
        request.setImageUrls(cleaned);
    }

    private String maskSensitive(String value) {
        String input = trimToNull(value);
        if (input == null) {
            return null;
        }
        String result = EMAIL_PATTERN.matcher(input).replaceAll("[已脱敏邮箱]");
        return PHONE_PATTERN.matcher(result).replaceAll("[已脱敏手机号]");
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
