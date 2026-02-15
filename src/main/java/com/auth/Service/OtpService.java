package com.auth.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;

    public String generateOtp(String phone) {

        String otp = String.valueOf(
                new Random().nextInt(900000) + 100000
        );

        redisTemplate.opsForValue()
                .set("otp:" + phone, otp, Duration.ofMinutes(5));

        return otp; // in real production send via SMS provider
    }

    public boolean verifyOtp(String phone, String otp) {

        String stored = redisTemplate.opsForValue()
                .get("otp:" + phone);

        if (stored != null && stored.equals(otp)) {
            redisTemplate.delete("otp:" + phone);
            return true;
        }

        return false;
    }
}
