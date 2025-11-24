package com.ticketguru.payment_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.payment-success}")
    private String paymentSuccessTopic;

    @Value("${kafka.topics.payment-failure}")
    private String paymentFailureTopic;

    public void sendPaymentSuccessfulEvent(Object event) {
        sendEvent(paymentSuccessTopic, event);
    }

    public void sendPaymentFailedEvent(Object event) {
        sendEvent(paymentFailureTopic, event);
    }

    private void sendEvent(String topic, Object event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, message);
            log.info("Kafka'ya '{}' topic'ine mesaj gönderildi: {}", topic, message);
        } catch (JsonProcessingException e) {
            log.error("Olay objesi JSON'a dönüştürülürken hata oluştu: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Kafka'ya mesaj gönderilirken hata oluştu: {}", e.getMessage(), e);
        }
    }
}