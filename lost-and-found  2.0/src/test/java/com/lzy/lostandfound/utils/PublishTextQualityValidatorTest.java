package com.lzy.lostandfound.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PublishTextQualityValidatorTest {

    @Test
    void validateItemNameShouldPassForNormalName() {
        assertNull(PublishTextQualityValidator.validateItemName("iPhone 8"));
    }

    @Test
    void validateItemNameShouldRejectOnlyNumber() {
        assertEquals("物品名称不能只填写数字", PublishTextQualityValidator.validateItemName("123456"));
    }

    @Test
    void validateItemNameShouldRejectNoiseWord() {
        assertEquals("物品名称疑似无效，请填写真实名称", PublishTextQualityValidator.validateItemName("asdf"));
    }

    @Test
    void validateLocationShouldPassForNormalLocation() {
        assertNull(PublishTextQualityValidator.validateLocation("图书馆三楼自习区"));
    }

    @Test
    void validateLocationShouldRejectOnlyNumber() {
        assertEquals("地点不能只填写数字", PublishTextQualityValidator.validateLocation("12345"));
    }

    @Test
    void validateLocationShouldRejectKeyboardSequence() {
        assertEquals("地点信息疑似无效，请填写具体地点", PublishTextQualityValidator.validateLocation("qwer"));
    }
}

