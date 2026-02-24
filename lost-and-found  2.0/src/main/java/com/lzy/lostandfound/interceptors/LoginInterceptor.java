package com.lzy.lostandfound.interceptors;

import com.lzy.lostandfound.utils.JwtUtil;
import com.lzy.lostandfound.utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component//
public class LoginInterceptor implements HandlerInterceptor {
    //令牌校验
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        try {
            //获取redis中的令牌信息
            ValueOperations<String, String> operations = redisTemplate.opsForValue();
            String redisToken = operations.get(token);
            if (redisToken == null) {
                //失效
                throw new RuntimeException("token失效");//抛出异常，被捕获
            }

            Map<String, Object> claims = JwtUtil.parseToken(token);//获取不到报错
            //把业务数据放到ThreadLocal中，供后续业务使用
            ThreadLocalUtil.set(claims);//记得删除
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
