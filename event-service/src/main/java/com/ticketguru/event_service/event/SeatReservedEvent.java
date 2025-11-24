package com.ticketguru.event_service.event;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class SeatReservedEvent {

    private Long eventId;
    private String seatNumber;
    private Long userId;
    private LocalDateTime reservationTime;

    public SeatReservedEvent(Long eventId, String seatNumber, Long userId, LocalDateTime reservationTime) {
        this.eventId = eventId;
        this.seatNumber = seatNumber;
        this.userId = userId;
        this.reservationTime = reservationTime;

    }

}