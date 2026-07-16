package com.offerpilot.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final String secret;
    private final long expirationMs;

    public JwtUtil(
            @Value("${offerpilot.jwt.secret}") String secret,
            @Value("${offerpilot.jwt.expiration}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(Long userId, String username) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + expirationMs);

        return JWT.create()
                .withClaim("userId", String.valueOf(userId))
                .withClaim("username", username)
                .withIssuedAt(now)
                .withExpiresAt(expireAt)
                .withIssuer("offer-pilot")
                .sign(algorithm);
    }

    /**
     * 验证 Token 并返回 DecodedJWT
     */
    public DecodedJWT verifyToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("offer-pilot")
                .build();
        return verifier.verify(token);
    }

    /**
     * 从 Token 中提取 userId
     */
    public Long getUserIdFromToken(String token) {
        DecodedJWT decoded = verifyToken(token);
        return Long.valueOf(decoded.getClaim("userId").asString());
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
