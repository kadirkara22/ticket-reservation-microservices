package com.ticketguru.payment_service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketguru.payment_service.event.PaymentFailedEvent;
import com.ticketguru.payment_service.event.PaymentSuccessfulEvent;
import com.ticketguru.payment_service.event.SeatReservedEvent;
import com.ticketguru.payment_service.model.PaymentStatus;
import com.ticketguru.payment_service.model.PaymentTransaction;
import com.ticketguru.payment_service.repository.PaymentTransactionRepository;
import com.ticketguru.payment_service.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationEventListener {

    private final ObjectMapper objectMapper;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final KafkaProducerService kafkaProducerService;

    private final Random random = new Random();

    @KafkaListener(topics = "${kafka.topics.reservation-initiation}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void listenSeatReservedEvent(String message) {
        log.info("Kafka mesajı alındı: {}", message);

        try {
            SeatReservedEvent event = objectMapper.readValue(message, SeatReservedEvent.class);
            log.info("Event işleniyor: {}", event);

            // 1. GÜVENLİK DUVARI: IDEMPOTENCY KONTROLÜ
            // Eğer bu bilet için daha önce işlem yapıldıysa, kendini tekrar etme!
            if (paymentTransactionRepository.existsByEventIdAndSeatId(event.getEventId(), event.getSeatNumber())) {
                log.warn("⚠️ Mükerrer işlem engellendi! Bu rezervasyon zaten işlenmiş. Event: {}, Seat: {}",
                        event.getEventId(), event.getSeatNumber());
                return; // Metottan çık, işlem yapma.
            }

            Double paymentAmount = event.getPrice() != null ? event.getPrice() : 50.0;
            String seatNumber = event.getSeatNumber() != null ? event.getSeatNumber() : "N/A";

            // Transaction nesnesini oluştur
            PaymentTransaction transaction = PaymentTransaction.builder()
                    .userId(event.getUserId())
                    .seatId(seatNumber)
                    .eventId(event.getEventId())
                    .amount(paymentAmount)
                    .status(PaymentStatus.PENDING)
                    .transactionTime(LocalDateTime.now())
                    // Benzersiz Referans Numarası (UUID daha güvenlidir)
                    .transactionRef(UUID.randomUUID().toString())
                    .build();

            // Ödeme Simülasyonu (%50 şans)
            // (Testlerde kolay hata görmek için bu oranı değiştirebilirsin)
           // boolean isPaymentSuccessful = random.nextInt(100) < 50;
            boolean isPaymentSuccessful = true;

            if (isPaymentSuccessful) {
                // BAŞARILI DURUM
                transaction.setStatus(PaymentStatus.SUCCESS);
                PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
                log.info("✅ Ödeme BAŞARILI, veritabanına kaydedildi. ID: {}", savedTransaction.getId());

                // Kafka Event Hazırlığı
                PaymentSuccessfulEvent successEvent = new PaymentSuccessfulEvent(
                        event.getEventId(),
                        seatNumber,
                        event.getUserId(),
                        savedTransaction.getId(),
                        savedTransaction.getAmount(),
                        savedTransaction.getTransactionTime()
                );

                // 2. KRİTİK NOKTA: TRANSACTIONAL OUTBOX PATTERN (Basitleştirilmiş)
                // DB Commit olmadan Kafka mesajı atma!
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("DB Commit oldu. Success Event Kafka'ya gönderiliyor...");
                        kafkaProducerService.sendPaymentSuccessfulEvent(successEvent);
                    }
                });

            } else {
                // BAŞARISIZ DURUM
                transaction.setStatus(PaymentStatus.FAILED);
                transaction.setFailureReason("Yetersiz Bakiye / Banka Reddi");
                PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);
                log.warn("❌ Ödeme BAŞARISIZ, veritabanına kaydedildi. ID: {}", savedTransaction.getId());

                PaymentFailedEvent failedEvent = new PaymentFailedEvent(
                        event.getEventId(),
                        seatNumber,
                        event.getUserId(),
                        savedTransaction.getId(),
                        savedTransaction.getAmount(),
                        savedTransaction.getFailureReason(),
                        savedTransaction.getTransactionTime()
                );

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("DB Commit oldu. Failure Event Kafka'ya gönderiliyor...");
                        kafkaProducerService.sendPaymentFailedEvent(failedEvent);
                    }
                });
            }

        } catch (JsonProcessingException e) {
            log.error("JSON Hatası: {}", message, e);
        } catch (Exception e) {
            log.error("Beklenmeyen Hata: {}", e.getMessage(), e);
            // Burada normalde mesajı Dead Letter Queue'ya (DLQ) atmamız gerekir.
            // İleride RabbitMQ veya Kafka DLQ ekleyince burayı güncelleyeceğiz.
        }
    }
}