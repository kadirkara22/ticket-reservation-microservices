package com.ticketguru.notificationservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationListener {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "${kafka.topics.payment-success}", groupId = "notification-group")
    public void handleNotification(String message) {
        log.info("Ödeme Başarılı Mesajı Geldi: {}", message);


        sendEmail(message);
    }

    private void sendEmail(String messageContent) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            mailMessage.setFrom("kadirkar2204@gmail.com");
            mailMessage.setTo("kadirkar2204@gmail.com");

            mailMessage.setSubject("🎟️ Ticket-Booking: Biletiniz Hazır!");
            mailMessage.setText("Sayın Müşterimiz,\n\nÖdemeniz başarıyla alınmıştır. İyi eğlenceler dileriz!\n\nDetaylar:\n" + messageContent);

            javaMailSender.send(mailMessage);

            log.info("GERÇEK EMAIL GÖNDERİLDİ!");
        } catch (Exception e) {
            log.error("Mail gönderilirken hata oluştu: {}", e.getMessage());
        }
    }}
