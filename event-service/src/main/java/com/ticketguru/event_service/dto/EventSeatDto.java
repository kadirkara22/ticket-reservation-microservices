package com.ticketguru.event_service.dto;

import com.ticketguru.event_service.model.SeatStatus;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSeatDto {
    private Long id;
    private Long eventId;      // Komple Event nesnesi yerine sadece ID
    private String seatNumber;
    private SeatStatus status;
    private BigDecimal price;
}