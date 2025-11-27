package com.ticketguru.event_service.controller;

import com.ticketguru.event_service.dto.EventSeatDto;
import com.ticketguru.event_service.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final SeatService seatService;

    @GetMapping("/{eventId}/seats")
    public ResponseEntity<List<EventSeatDto>> getSeats(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatService.getSeatsByEvent(eventId));
    }

    @PostMapping("/seats/{seatId}/reserve")
    public ResponseEntity<String> reserveSeat(
            @PathVariable Long seatId,
            @RequestParam Long userId) {

        // Service katmanı exception fırlatırsa GlobalExceptionHandler yakalayacak
        String result = seatService.reserveSeat(seatId, userId);
        return ResponseEntity.ok(result);
    }
}