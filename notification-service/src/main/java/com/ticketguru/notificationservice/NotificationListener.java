package com.ticketguru.notificationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketguru.notificationservice.client.UserServiceClient;
import com.ticketguru.notificationservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final JavaMailSender javaMailSender;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @KafkaListener(topics = "${kafka.topics.payment-success}", groupId = "notification-group")
    public void handleNotification(String message) {
        log.info("💰 Ödeme Mesajı Geldi: {}", message);

        try {
            // 1. Gelen JSON mesajından userId'yi bulmamız lazım.
            // Mesaj örneği: {"eventId":1, "userId":5, "amount":50.0 ...}
            // Bunu basitçe Map'e çevirip alalım.
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);

            // userId bazen Integer bazen Long gelebilir, güvenli çevirelim
            Long userId = Long.valueOf(eventData.get("userId").toString());

            // 2. User Service'i ara ve emaili iste
            log.info("🔍 User Service'den {} ID'li kullanıcı aranıyor...", userId);
            UserDto userDto = userServiceClient.getUserById(userId);

            String userEmail = userDto.getEmail();
            log.info("📧 Kullanıcı Email Bulundu: {}", userEmail);

            // 3. O Emaile Gönder
            sendEmail(userEmail, message);

        } catch (Exception e) {
            log.error("Hata oluştu: {}", e.getMessage());
        }
    }

    private void sendEmail(String toEmail, String messageContent) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(toEmail);
            mailMessage.setSubject("🎟️ Biletiniz Hazır!");
            mailMessage.setText("Tebrikler! Bilet alma işleminiz tamamlandı.\n\nDetaylar:\n" + messageContent);

            javaMailSender.send(mailMessage);
            log.info("✅ Mail başarıyla {} adresine gönderildi! (Gönderen: {})", toEmail, fromEmail);
        } catch (Exception e) {
            log.error("Mail atılamadı: {}", e.getMessage());
        }
    }
}