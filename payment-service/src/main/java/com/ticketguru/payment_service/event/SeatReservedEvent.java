package com.ticketguru.payment_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservedEvent {
    private String eventId;
    private Long userId;
    private String seatNumber;
    private LocalDateTime reservationTime;
    private Double price;
    private LocalDateTime timestamp;
}