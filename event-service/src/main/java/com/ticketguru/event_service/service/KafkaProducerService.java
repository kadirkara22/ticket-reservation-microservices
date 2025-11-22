package com.ticketguru.event_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketguru.event_service.dto.SeatReservedEvent;
import lombok.RequiredArgsConstructor; // Lombok'u ekleyin
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException; // .get() için

@Service
@RequiredArgsConstructor // KafkaTemplate ve ObjectMapper DI için
@Slf4j
@ConditionalOnProperty(name = "kafka.enabled", havingValue = "true", matchIfMissing = false)
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // Spring Boot bu bean'i otomatik yarat?r

    @Value("${kafka.topics.reservation-initiation}")
    private String topicName;

    public void sendSeatReservedEvent(SeatReservedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event); // DTO'yu JSON'a çevir

            // Senkron gönderim ile hatay? yakalama (Test amaçl?)
            kafkaTemplate.send(topicName, event.getSeatId().toString(), message).get();

            log.info("Kafka'ya BAŞARILI GÖNDERİM: Topic={}, SeatId={}, Payload={}", topicName, event.getSeatId(), message);

        } catch (JsonProcessingException e) {
            log.error("Event nesnesi JSON'a çevrilirken hata: {}", event, e);
        } catch (InterruptedException | ExecutionException e) {
            log.error("KAFKA GÖNDERİM HATASI: Mesaj gönderilemedi!", e); // Hata logunu takip edin!
        }
    }
}