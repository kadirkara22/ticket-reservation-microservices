package com.ticketguru.event_service.repository;

import com.ticketguru.event_service.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
