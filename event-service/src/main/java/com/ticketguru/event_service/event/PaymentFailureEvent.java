package com.ticketguru.event_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailureEvent {
    private String eventId;
    private String seatId;
    private Long userId;
    private Long transactionId;
    private Double amount;
    private String failureReason;
    private LocalDateTime paymentTime;
}