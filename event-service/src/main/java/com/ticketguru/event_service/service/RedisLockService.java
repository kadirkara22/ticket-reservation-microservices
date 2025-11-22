package com.ticketguru.event_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    // Koltuğu belirli bir süre (örn: 10 dakika) kilitle
    public boolean lockSeat(Long eventId, String seatNumber, Long userId) {
        // Key: lock:eventId:seatNumber -> Value: userId
        String key = "lock:" + eventId + ":" + seatNumber;

        // setIfAbsent = Redis komutu: SETNX (Set if Not Exists)
        // Eğer key yoksa yaz ve true dön. Varsa hiçbir şey yapma ve false dön.
        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(key, String.valueOf(userId), Duration.ofMinutes(10)); // 10 dakika süre

        return Boolean.TRUE.equals(success);
    }

    // Kilidi kaldır (Ödeme başarısız veya iptal edilirse)
    public void unlockSeat(Long eventId, String seatNumber) {
        String key = "lock:" + eventId + ":" + seatNumber;
        redisTemplate.delete(key);
    }
}