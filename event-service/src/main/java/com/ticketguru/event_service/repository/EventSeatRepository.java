    package com.ticketguru.event_service.repository;

    import com.ticketguru.event_service.model.EventSeat;
    import com.ticketguru.event_service.model.SeatStatus;
    import io.lettuce.core.dynamic.annotation.Param;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.data.jpa.repository.Query;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {


        @Query("SELECT s FROM EventSeat s JOIN FETCH s.event WHERE s.event.id = :eventId ORDER BY s.id")
        List<EventSeat> findByEventIdWithFetch(@Param("eventId") Long eventId);

        List<EventSeat> findByStatusAndLockTimeBefore(SeatStatus status, LocalDateTime lockTime);

        Optional<EventSeat> findByEventIdAndSeatNumber(Long eventId, String seatNumber);
    }
