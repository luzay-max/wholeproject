package com.lzy.lostandfound.utils;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 发布文本质量校验：用于拦截明显乱填的物品名称和地点。
 */
public final class PublishTextQualityValidator {

    private static final Pattern MEANINGFUL_PATTERN = Pattern.compile("[\\p{IsHan}A-Za-z0-9]");
    private static final Pattern ONLY_NUMBER_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern REPEAT_BLOCK_PATTERN = Pattern.compile("^(\\d{2,})\\1+$");
    private static final Pattern SAME_CHAR_PATTERN = Pattern.compile("^(.)\\1{2,}$");

    private static final Set<String> NOISE_WORDS = Set.of(
            "test", "ceshi", "asdf", "qwer", "zxcv", "abc", "abcd", "abcde",
            "qwerty", "asdfgh", "aaaa", "bbbb", "cccc", "xxxx", "yyyy", "zzzz",
            "1111", "1234", "12345", "123456", "6666", "9999"
    );

    private static final String[] KEYBOARD_ROWS = {
            "qwertyuiop",
            "asdfghjkl",
            "zxcvbnm",
            "1234567890"
    };

    private PublishTextQualityValidator() {
    }

    public static String validateItemName(String value) {
        String text = normalize(value);
        if (!StringUtils.hasText(text)) {
            return "物品名称不能为空";
        }
        if (text.length() > 50) {
            return "物品名称不能超过50个字符";
        }
        if (!MEANINGFUL_PATTERN.matcher(text).find()) {
            return "物品名称格式不正确";
        }
        if (ONLY_NUMBER_PATTERN.matcher(compact(text)).matches()) {
            return "物品名称不能只填写数字";
        }
        if (isLikelyNoise(text)) {
            return "物品名称疑似无效，请填写真实名称";
        }
        return null;
    }

    public static String validateLocation(String value) {
        String text = normalize(value);
        if (!StringUtils.hasText(text)) {
            return "地点不能为空";
        }
        if (text.length() < 2) {
            return "地点至少填写2个字符";
        }
        if (text.length() > 100) {
            return "地点不能超过100个字符";
        }
        if (!MEANINGFUL_PATTERN.matcher(text).find()) {
            return "地点格式不正确";
        }
        if (ONLY_NUMBER_PATTERN.matcher(compact(text)).matches()) {
            return "地点不能只填写数字";
        }
        if (isLikelyNoise(text)) {
            return "地点信息疑似无效，请填写具体地点";
        }
        return null;
    }

    private static boolean isLikelyNoise(String value) {
        String compact = compact(value);
        if (!StringUtils.hasText(compact)) {
            return true;
        }
        String lower = compact.toLowerCase(Locale.ROOT);

        if (NOISE_WORDS.contains(lower)) {
            return true;
        }
        if (SAME_CHAR_PATTERN.matcher(lower).matches()) {
            return true;
        }
        if (REPEAT_BLOCK_PATTERN.matcher(lower).matches()) {
            return true;
        }
        return isKeyboardSequence(lower);
    }

    private static boolean isKeyboardSequence(String value) {
        if (!StringUtils.hasText(value) || value.length() < 4) {
            return false;
        }
        for (String row : KEYBOARD_ROWS) {
            String reversed = new StringBuilder(row).reverse().toString();
            for (int i = 0; i <= value.length() - 4; i++) {
                String segment = value.substring(i, i + 4);
                if (row.contains(segment) || reversed.contains(segment)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String compact(String value) {
        return normalize(value).replaceAll("\\s+", "");
    }
}

