package com.ticketguru.event_service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketguru.event_service.event.PaymentFailureEvent;
import com.ticketguru.event_service.event.PaymentSuccessfulEvent;
import com.ticketguru.event_service.listener.PaymentResultEventListener;
import com.ticketguru.event_service.service.SeatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentResultEventListenerTest {

    @Mock
    private SeatService seatService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentResultEventListener listener;

    private final String paymentSuccessTopic = "payment-success-topic";
    private final String paymentFailureTopic = "payment-failure-topic";

    @BeforeEach
    void setUp() {
        // @Value ile enjekte edilen alanları Mockito doğrudan yönetemez.
        // ReflectionTestUtils kullanarak manuel olarak set ediyoruz.
        ReflectionTestUtils.setField(listener, "paymentSuccessTopic", paymentSuccessTopic);
        ReflectionTestUtils.setField(listener, "paymentFailureTopic", paymentFailureTopic);
    }

    @Test
    void listenPaymentSuccessfulEvent_shouldCallMarkSeatAsPaid_whenPaymentIsSuccessful() throws JsonProcessingException {

        String kafkaMessage = "{\"eventId\":\"1\",\"seatId\":\"A-3-25\",\"userId\":99,\"transactionId\":55,\"amount\":50.0,\"paymentTime\":\"2025-11-23T20:28:26.312676\"}";
        PaymentSuccessfulEvent successfulEvent = PaymentSuccessfulEvent.builder()
                .eventId("1")
                .seatId("A-3-25")
                .userId(99L)
                .transactionId(55L)
                .amount(50.0)
                .paymentTime(LocalDateTime.parse("2025-11-23T20:28:26.312676"))
                .build();

        // objectMapper'in belirli bir mesaj aldığında belirli bir event objesi döndürmesini sağlıyoruz
        when(objectMapper.readValue(kafkaMessage, PaymentSuccessfulEvent.class)).thenReturn(successfulEvent);

        // Eylem (Act)
        listener.listenPaymentSuccessfulEvent(kafkaMessage);

        // Doğrulama (Assert)
        // seatService.markSeatAsPaid metodunun doğru parametrelerle çağrıldığını doğrula
        verify(seatService, times(1)).markSeatAsPaid("1", "A-3-25", 99L);
        // Başka hiçbir metodun çağrılmadığını da doğrulayabiliriz, ama bu test için bu yeterli.
        verifyNoMoreInteractions(seatService);
    }

    @Test
    void listenPaymentSuccessfulEvent_shouldHandleJsonProcessingException() throws JsonProcessingException {

        String invalidKafkaMessage = "invalid json";

        // objectMapper'in JSON parse hatası fırlatmasını sağlıyoruz
        when(objectMapper.readValue(invalidKafkaMessage, PaymentSuccessfulEvent.class)).thenThrow(JsonProcessingException.class);

        // Eylem (Act)
        listener.listenPaymentSuccessfulEvent(invalidKafkaMessage);

        // Doğrulama (Assert)
        // seatService üzerinde hiçbir metodun çağrılmadığını doğrula
        verifyNoInteractions(seatService);
    }

    @Test
    void listenPaymentFailedEvent_shouldCallMarkSeatAsAvailable_whenPaymentIsFailed() throws JsonProcessingException {
        // Haz?rl?k (Arrange)
        String kafkaMessage = "{\"eventId\":\"1\",\"seatId\":\"A-3-10\",\"userId\":88,\"transactionId\":54,\"amount\":50.0,\"failureReason\":\"Insufficient funds or payment declined.\",\"paymentTime\":\"2025-11-23T20:28:09.763540\"}";
        PaymentFailureEvent failureEvent = PaymentFailureEvent.builder()
                .eventId("1")
                .seatId("A-3-10")
                .userId(88L)
                .transactionId(54L)
                .amount(50.0)
                .failureReason("Insufficient funds or payment declined.")
                .paymentTime(LocalDateTime.parse("2025-11-23T20:28:09.763540"))
                .build();

        // objectMapper'in belirli bir mesaj aldığında belirli bir event objesi döndürmesini sağlıyoruz
        when(objectMapper.readValue(kafkaMessage, PaymentFailureEvent.class)).thenReturn(failureEvent);

        // Eylem (Act)
        listener.listenPaymentFailedEvent(kafkaMessage);

        // Do?rulama (Assert)
        // seatService.markSeatAsAvailable metodunun do?ru parametrelerle çağrıldığını doğrula
        verify(seatService, times(1)).markSeatAsAvailable("1", "A-3-10");
        // Ba?ka hiçbir metodun çağrılmadığını da doğrulayabiliriz
        verifyNoMoreInteractions(seatService); // Hem markSeatAsPaid hem de markSeatAsAvailable'in doğrulanmasını kontrol eder.
        // E?er listenPaymentSuccessfulEvent daha önce çağrılmadıysa, sorun olmaz.
    }

    @Test
    void listenPaymentFailedEvent_shouldHandleJsonProcessingException() throws JsonProcessingException {

        String invalidKafkaMessage = "invalid json";

        // objectMapper'in JSON parse hatası fırlatmasını sağlıyoruz
        when(objectMapper.readValue(invalidKafkaMessage, PaymentFailureEvent.class)).thenThrow(JsonProcessingException.class);

        // Eylem (Act)
        listener.listenPaymentFailedEvent(invalidKafkaMessage);

        // Doğrulama (Assert)
        // seatService üzerinde hiçbir metodun çağrılmadığını doğrula
        verifyNoInteractions(seatService);
    }
}