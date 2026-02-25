package com.lzy.lostandfound.aop;

import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.entity.OperationLog;
import com.lzy.lostandfound.service.IOperationLogService;
import com.lzy.lostandfound.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.lzy.lostandfound.vo.Result;
import com.lzy.lostandfound.vo.Tu;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)1\\d{10}(?!\\d)");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Za-z0-9._%+-]{1,64}@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final int MAX_COLLECTION_ITEMS = 20;
    private static final int MAX_STRING_LENGTH = 300;

    @Autowired
    private IOperationLogService operationLogService;
    @Autowired
    private IUserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Pointcut("@annotation(com.lzy.lostandfound.anno.Log)")
    public void logPointcut() {}

    @Around("logPointcut() && @annotation(logAnno)")
    public Object around(ProceedingJoinPoint pjp, Log logAnno) throws Throwable {
        // 先获取ThreadLocal中的用户信息，防止proceed后ThreadLocal被清理
        Map<String, Object> tl = ThreadLocalUtil.get();
        String operatorId = tl != null && tl.get("id") != null ? tl.get("id").toString() : null;

        Object ret = pjp.proceed();
        try {
            String operationType = logAnno.value() != null ? logAnno.value() : "";
            HttpServletRequest request = currentRequest();
            String ip = request != null ? request.getRemoteAddr() : "unknown";

            MethodSignature ms = (MethodSignature) pjp.getSignature();
            String method = ms.getMethod().getName();
            String uri = request != null ? request.getRequestURI() : "";

            Map<String, Object> detail = new HashMap<>();
            detail.put("uri", uri);
            detail.put("method", method);
            detail.put("operationType", operationType);
            detail.put("timestamp", LocalDateTime.now().toString());
            Map<String, Object> args = extractArgs(ms.getParameterNames(), pjp.getArgs());
            detail.put("args", args);
            if (ret instanceof Result r) {
                detail.put("resultCode", r.getCode());
                detail.put("resultMessage", r.getMessage());
            }

            OperationLog ol = new OperationLog();
            Long uid = null;
            if ("LOGIN".equalsIgnoreCase(operationType) && ret instanceof Result<?> r) {
                Object data = r.getData();
                if (data instanceof Tu tu && tu.getUser() != null && tu.getUser().getId() != null) {
                    try { uid = Long.valueOf(tu.getUser().getId()); } catch (Exception e) { /* ignore */ }
                    detail.put("username", tu.getUser().getUsername());
                    detail.put("accountName", tu.getUser().getAccountName());
                }
            }
            if (uid == null && operatorId != null) {
                try {
                    uid = Long.valueOf(operatorId);
                } catch (Exception ignored) {
                    // ignore
                }
            }
            ol.setUserId(uid);
            ol.setActionType(operationType);
            detail.put("ip", ip);
            if (uid != null) {
                detail.put("userId", uid);
            }
            ol.setContent(objectMapper.writeValueAsString(detail));
            ol.setCreateTime(LocalDateTime.now());
            
            // 同步保存日志，确保数据写入
            try {
                // 如果userId为空，设置为-1（系统/未知），防止数据库报错
                if (ol.getUserId() == null) {
                    ol.setUserId(-1L);
                }
                boolean saveSuccess = operationLogService.save(ol);
                if (!saveSuccess) {
                    log.warn("操作日志保存失败，operationType={}", operationType);
                }
            } catch (Exception e) {
                log.warn("操作日志保存异常，operationType={}", operationType, e);
            }
        } catch (Exception e) {
            log.warn("记录操作日志异常", e);
        }
        return ret;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }

    private Map<String, Object> extractArgs(String[] names, Object[] values) {
        Map<String, Object> map = new HashMap<>();
        if (names == null || values == null) return map;
        for (int i = 0; i < names.length && i < values.length; i++) {
            String name = names[i];
            if (name == null) continue;
            if (isSensitiveKey(name)) continue;
            map.put(name, sanitizeArgValue(values[i]));
        }
        return map;
    }

    private boolean isSensitiveKey(String key) {
        String lower = key.toLowerCase();
        return lower.contains("password")
                || lower.contains("captcha")
                || lower.contains("token")
                || lower.contains("authorization")
                || lower.contains("phone")
                || lower.contains("mobile")
                || lower.contains("email")
                || lower.contains("contact")
                || lower.contains("studentid")
                || lower.contains("idcard")
                || lower.contains("secret");
    }

    private Object sanitizeArgValue(Object value) {
        if (value == null) return null;
        if (value instanceof HttpServletRequest) return "HttpServletRequest";
        if (value instanceof MultipartFile file) return "MultipartFile(" + safeText(file.getOriginalFilename()) + ")";
        if (value instanceof Number || value instanceof Boolean) return value;

        if (value instanceof Map<?, ?> src) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            int count = 0;
            for (Map.Entry<?, ?> entry : src.entrySet()) {
                if (count >= MAX_COLLECTION_ITEMS) {
                    sanitized.put("...", "...");
                    break;
                }
                String key = String.valueOf(entry.getKey());
                if (isSensitiveKey(key)) {
                    sanitized.put(key, "[MASKED]");
                } else {
                    sanitized.put(key, sanitizeArgValue(entry.getValue()));
                }
                count++;
            }
            return sanitized;
        }

        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            int count = 0;
            for (Object item : iterable) {
                if (count >= MAX_COLLECTION_ITEMS) {
                    result.add("...");
                    break;
                }
                result.add(sanitizeArgValue(item));
                count++;
            }
            return result;
        }

        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> result = new ArrayList<>();
            int max = Math.min(length, MAX_COLLECTION_ITEMS);
            for (int i = 0; i < max; i++) {
                result.add(sanitizeArgValue(Array.get(value, i)));
            }
            if (length > MAX_COLLECTION_ITEMS) {
                result.add("...");
            }
            return result;
        }

        return safeText(String.valueOf(value));
    }

    private String safeText(String text) {
        if (text == null) return "";
        String masked = EMAIL_PATTERN.matcher(text).replaceAll("[已脱敏邮箱]");
        masked = PHONE_PATTERN.matcher(masked).replaceAll("[已脱敏手机号]");
        if (masked.length() > MAX_STRING_LENGTH) {
            return masked.substring(0, MAX_STRING_LENGTH) + "...";
        }
        return masked;
    }
}
