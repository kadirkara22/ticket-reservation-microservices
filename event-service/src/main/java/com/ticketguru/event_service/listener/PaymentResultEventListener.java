package com.ticketguru.event_service.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketguru.event_service.event.PaymentFailureEvent;
import com.ticketguru.event_service.event.PaymentSuccessfulEvent;
import com.ticketguru.event_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentResultEventListener {

    private final SeatService seatService;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payment-success}")
    private String paymentSuccessTopic;

    @Value("${kafka.topics.payment-failure}")
    private String paymentFailureTopic;

    @KafkaListener(topics = "${kafka.topics.payment-success}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenPaymentSuccessfulEvent(String message) {
        log.info("DEBUG: listenPaymentSuccessfulEvent metodu tetiklendi. Dinlenen topic: {}", paymentSuccessTopic);
        try {
            log.info("Kafka'dan '{}' topic'inden mesaj alındı: {}", paymentSuccessTopic, message);

            PaymentSuccessfulEvent event = objectMapper.readValue(message, PaymentSuccessfulEvent.class);
            log.info("PaymentSuccessfulEvent objesine dönüştürüldü: {}", event);

            seatService.markSeatAsPaid(
                    event.getEventId(),
                    event.getSeatId(),
                    event.getUserId()
            );
            log.info("Koltuk başarılı ödeme sonrası sold edildi: EventId={}, SeatId={}, UserId={}", event.getEventId(), event.getSeatId(), event.getUserId());

        } catch (JsonProcessingException e) {
            log.error("Kafka mesajı işlenirken JSON parse hatası oluştu: {}", message, e);
        } catch (Exception e) {
            log.error("PaymentSuccessfulEvent işlenirken beklenmeyen bir hata oluştu: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${kafka.topics.payment-failure}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenPaymentFailedEvent(String message) {
        try {

            PaymentFailureEvent event = objectMapper.readValue(message, PaymentFailureEvent.class);

            seatService.markSeatAsAvailable(event.getEventId(), event.getSeatId());
            log.info("Koltuk başarısız ödeme sonrası 'AVAILABLE' olarak güncellendi: EventId={}, SeatId={}", event.getEventId(), event.getSeatId());
        } catch (Exception e) {
            log.error("PaymentFailureEvent işlenirken hata: {}", e.getMessage(), e);
        }
    }
}