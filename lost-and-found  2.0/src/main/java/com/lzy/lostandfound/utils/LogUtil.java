package com.lzy.lostandfound.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 统一日志工具，避免在业务代码中直接使用 printStackTrace/System.out。
 */
@Slf4j
public final class LogUtil {

    private LogUtil() {
    }

    public static void info(String message) {
        log.info(message);
    }

    public static void warn(String message) {
        log.warn(message);
    }

    public static void warn(String message, Throwable throwable) {
        log.warn(message, throwable);
    }

    public static void error(String message) {
        log.error(message);
    }

    public static void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }
}

