package com.ticketguru.notificationservice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j // Loglama için (System.out.println yerine log.info kullanacağız)
public class NotificationListener {

    // "notificationTopic" adlı konuyu dinle
    @KafkaListener(topics = "${kafka.topics.payment-success}", groupId = "notification-group")
    public void handleNotification(String message) {
        log.info("📨 KAFKA'DAN MESAJ GELDİ: {}", message);

        // Simülasyon: Mail atılıyor gibi yapalım
        log.info("📧 Kullanıcıya email gönderiliyor... İçerik: {}", message);
        log.info("✅ Email başarıyla gönderildi!");
    }
}
