package com.ticketguru.event_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    // Koltuğu atomik olarak kilitle (Kilit + Süre tek komutta)
    public boolean lockSeat(Long eventId, String seatNumber, Long userId) {
        String key = "lock:" + eventId + ":" + seatNumber;

        log.info("Redis Kilit Denemesi -> KEY: {} | USER: {}", key, userId);


        // setIfAbsent metodu 3 parametre alıyor: Key, Value, Duration (Süre)
        // Bu işlem Redis tarafında ATOMİK olarak çalışır.
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(userId), Duration.ofMinutes(1));

        if (Boolean.TRUE.equals(success)) {
            log.info("Kilit BAŞARIYLA alındı. Key: {}, Owner: {}, TTL: 10 dk", key, userId);
        } else {
            // Kimin kilitlediğini loglayalım (Debug için iyi olur)
            String currentOwner = redisTemplate.opsForValue().get(key);
            log.warn("Kilit ALINAMADI! Key: {}, Mevcut Sahip: {}", key, currentOwner);
        }

        log.info("Redis Sonuç (KEY: {}): {}", key, success);

        return Boolean.TRUE.equals(success);
    }

    public void unlockSeat(Long eventId, String seatNumber) {
        String key = "lock:" + eventId + ":" + seatNumber;
        redisTemplate.delete(key);
        log.info("Kilit kaldırıldı: {}", key);
    }
}