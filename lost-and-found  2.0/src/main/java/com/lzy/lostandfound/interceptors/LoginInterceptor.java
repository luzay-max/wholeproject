package com.lzy.lostandfound.interceptors;

import com.lzy.lostandfound.entity.User;
import com.lzy.lostandfound.service.IUserService;
import com.lzy.lostandfound.utils.JwtUtil;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;

@Component//
public class LoginInterceptor implements HandlerInterceptor {
    //令牌校验
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private IUserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        try {
            if (token == null || token.isBlank()) {
                response.setStatus(401);
                return false;
            }

            //获取redis中的令牌信息
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                //失效
                throw new RuntimeException("token失效");//抛出异常，被捕获
            }

            Map<String, Object> claims = JwtUtil.parseToken(token);//获取不到报错
            if (claims == null || claims.get("id") == null) {
                response.setStatus(401);
                return false;
            }

            String currentUserId = String.valueOf(claims.get("id"));
            User currentUser = userService.getById(currentUserId);
            if (currentUser == null) {
                response.setStatus(401);
                return false;
            }
            if (currentUser.getStatus() != null && currentUser.getStatus() == 1) {
                response.setStatus(403);
                return false;
            }

            // 后端强制权限：所有 /api/admin/** 接口必须管理员角色
            String requestUri = request.getRequestURI();
            if (requestUri != null && requestUri.startsWith("/api/admin/")
                    && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                response.setStatus(403);
                return false;
            }

            Map<String, Object> claimsWithRole = new HashMap<>(claims);
            claimsWithRole.put("role", currentUser.getRole());
            claimsWithRole.put("id", currentUser.getId());
            //把业务数据放到ThreadLocal中，供后续业务使用
            ThreadLocalUtil.set(claimsWithRole);//记得删除
           return true;//放星
        } catch (Exception e) {
            response.setStatus((401));
            return false;//不放行
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();//记得删除,防止内存泄露
    }
}
