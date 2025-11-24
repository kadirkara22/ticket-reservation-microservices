package com.ticketguru.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessfulEvent {
    private String eventId;
    private String seatId;
    private Long userId;
    private Long transactionId;
    private Double amount;
    private LocalDateTime paymentTime;

}