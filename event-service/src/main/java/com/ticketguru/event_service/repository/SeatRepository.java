package com.ticketguru.event_service.repository;

import com.ticketguru.event_service.model.EventSeat;
import com.ticketguru.event_service.model.EventSeat;
import com.ticketguru.event_service.model.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SeatRepository extends JpaRepository<EventSeat, Long> {

    List<EventSeat> findByStatusAndLockTimeBefore(SeatStatus status, LocalDateTime lockTime);
}