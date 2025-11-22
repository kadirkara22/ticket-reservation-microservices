package com.ticketguru.event_service.mapper;

import com.ticketguru.event_service.dto.EventSeatDto;
import com.ticketguru.event_service.model.EventSeat;
import org.springframework.stereotype.Component;

@Component
public class SeatMapper {

    public EventSeatDto toDto(EventSeat seat) {
        if (seat == null) return null;

        return EventSeatDto.builder()
                .id(seat.getId())
                .eventId(seat.getEvent().getId())
                .seatNumber(seat.getSeatNumber())
                .status(seat.getStatus())
                .price(seat.getPrice())
                .build();
    }
}