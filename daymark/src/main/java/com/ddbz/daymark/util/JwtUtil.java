package com.ddbz.daymark.util;

import com.ddbz.daymark.common.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component   // 声明为 Spring 组件，之后可以在 Service 里 @Autowired 注入
public class JwtUtil {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 生成 token
     * @param uid 用户ID（放进 token 的 sub 字段）
     * @return 签好名的 token 字符串
     */
    public String generateToken(String uid,int role) {
        return Jwts.builder()
                .subject(uid)                                        // sub = 用户username
                .claim("role",role)
                .issuedAt(new Date())                                   // iat = 签发时间（现在）
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpire())) // exp = 当前时间 + expire 毫秒
                .signWith(getKey())                                     // 用密钥签名
                .compact();                                             // 生成最终字符串
    }

    /**
     * 解析 token，取出用户uid
     * @param token 请求头里拿到的 token
     * @return 用户ID；如果 token 无效/过期会抛异常
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 私有方法：把配置里的密钥字符串转成 SecretKey
     */
    private SecretKey getKey() {
        // hmacShaKeyFor：要求字节数组长度 >= 32，否则抛 WeakKeyException
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
