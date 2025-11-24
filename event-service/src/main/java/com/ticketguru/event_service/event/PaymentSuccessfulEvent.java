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
public class PaymentSuccessfulEvent {
    private String eventId;
    private String seatId;
    private Long userId;
    private Long transactionId;
    private Double amount;
    private LocalDateTime paymentTime;
}