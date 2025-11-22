package com.ticketguru.event_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatReservedEvent {
    private Long seatId;
    private Long eventId;
    private Long userId;
    private LocalDateTime timestamp;
}