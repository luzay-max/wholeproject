package com.lzy.lostandfound.aop;

import com.lzy.lostandfound.anno.Log;
import com.lzy.lostandfound.entity.OperationLog;
import com.lzy.lostandfound.service.IOperationLogService;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.vo.Result;
import com.lzy.lostandfound.vo.Tu;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Aspect
@Component
public class OperationLogAspect {

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
            System.out.println("DEBUG: Entering OperationLogAspect logic"); // Debug log
            String operationType = logAnno.value() != null ? logAnno.value() : "";
            System.out.println("DEBUG: Operation Type: " + operationType); // Debug log
            
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
                } catch (Exception e) { 
                    System.err.println("DEBUG: Failed to parse operatorId: " + operatorId); // Debug log
                    /* ignore */ 
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
                    System.out.println("DEBUG: UserId is null, setting to -1"); // Debug log
                    ol.setUserId(-1L);
                }
                System.out.println("DEBUG: Saving OperationLog: " + ol); // Debug log
                boolean saveSuccess = operationLogService.save(ol);
                if (!saveSuccess) {
                    System.err.println("OperationLog save failed for: " + operationType);
                } else {
                    System.out.println("DEBUG: OperationLog saved successfully"); // Debug log
                }
            } catch (Exception e) {
                System.err.println("Error saving operation log: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("DEBUG: Exception in Aspect: " + e.getMessage()); // Debug log
            e.printStackTrace();
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
            if (name.toLowerCase().contains("password")) continue;
            if (name.toLowerCase().contains("captcha")) continue;
            map.put(name, values[i]);
        }
        return map;
    }
}
