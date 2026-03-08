package com.example.student.university_career_platform.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    // 32字节密钥，适配 HMAC-SHA256
    private static final String SECRET = "12345678901234567890123456789012";
    private static final long EXPIRATION_MS = 86400000; // 24小时

    // 0.12.x 标准密钥生成方式
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    // 生成 Token（0.12.x 仅支持 subject()）
    public String generateToken(String phone) {
        return Jwts.builder()
                .subject(phone)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 提取手机号（0.12.x 必须用 parser() -> verifyWith() -> build()）
    public String extractPhone(String token) {
        // 注意：Jwts.parser() 返回的是 JwtParserBuilder，所以需要先调用 verifyWith() 和 build()
        Claims claims = Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    // 校验 Token
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}