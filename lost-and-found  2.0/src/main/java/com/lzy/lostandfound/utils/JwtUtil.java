package com.lzy.lostandfound.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private static String key = "PLEASE_CHANGE_ME_JWT_SECRET";
    private static long expireMillis = 1000L * 60 * 60 * 12;

    @Value("${jwt.secret:PLEASE_CHANGE_ME_JWT_SECRET}")
    public void setKey(String configuredKey) {
        if (configuredKey != null && !configuredKey.isBlank()) {
            key = configuredKey;
        }
    }

    @Value("${jwt.expire-hours:12}")
    public void setExpireHours(long expireHours) {
        long safeHours = expireHours > 0 ? expireHours : 12;
        expireMillis = safeHours * 60 * 60 * 1000;
    }

	//接收业务数据,生成token并返回
    public static String genToken(Map<String, Object> claims) {
        return JWT.create()
                .withClaim("claims", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + expireMillis))
                .sign(Algorithm.HMAC256(key));
    }

	//接收token,验证token,并返回业务数据
    public static Map<String, Object> parseToken(String token) {
        return JWT.require(Algorithm.HMAC256(key))
                .build()
                .verify(token)
                .getClaim("claims")
                .asMap();
    }

}
