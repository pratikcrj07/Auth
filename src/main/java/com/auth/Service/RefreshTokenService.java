package com.auth.Service;

import com.auth.Config.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    public String createRefreshToken(String username) {

        String refreshToken = jwtUtil.generateRefreshToken(username);

        redisTemplate.opsForValue().set(
                "refresh:" + username,
                refreshToken,
                Duration.ofMillis(refreshExpiration)
        );

        return refreshToken;
    }

    public boolean validateRefreshToken(String username, String token) {
        String stored = redisTemplate.opsForValue().get("refresh:" + username);
        return token.equals(stored);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete("refresh:" + username);
    }

    public void blacklistAccessToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(
                "blacklist:" + token,
                "true",
                Duration.ofMillis(expirationMillis)
        );
    }
}
