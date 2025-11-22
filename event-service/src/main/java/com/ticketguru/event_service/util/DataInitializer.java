package com.ticketguru.event_service.util;

import com.ticketguru.event_service.model.*;
import com.ticketguru.event_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;

    @Override
    public void run(String... args) throws Exception {
        // Eğer veritabanında hiç etkinlik yoksa veri ekle
        if (eventRepository.count() == 0) {

            // 1. Mekan Oluştur
            Venue venue = Venue.builder()
                    .name("Harbiye Cemil Topuzlu Açıkhava Tiyatrosu")
                    .address("Nişantaşı, Şişli/İstanbul")
                    .capacity(5000)
                    .build();
            venueRepository.save(venue);

            // 2. Etkinlik Oluştur
            Event event = Event.builder()
                    .name("Tarkan Yaz Konserleri")
                    .eventDate(LocalDateTime.now().plusDays(30)) // 1 ay sonra
                    .venue(venue)
                    .build();
            eventRepository.save(event);

            // 3. Koltukları Oluştur (1000 Adet)
            // A, B, C blokları, her blokta 10 sıra, her sırada 33 koltuk ~ 1000 koltuk
            List<EventSeat> seats = new ArrayList<>();
            String[] blocks = {"A", "B", "C"};

            System.out.println("Koltuklar oluşturuluyor, lütfen bekleyiniz...");

            for (String block : blocks) {
                for (int row = 1; row <= 10; row++) {
                    for (int number = 1; number <= 33; number++) {
                        EventSeat seat = EventSeat.builder()
                                .event(event)
                                .seatNumber(block + "-" + row + "-" + number) // Örn: A-1-15
                                .status(SeatStatus.AVAILABLE)
                                .price(new BigDecimal("500.00")) // 500 TL
                                .build();
                        seats.add(seat);
                    }
                }
            }
            eventSeatRepository.saveAll(seats);
            System.out.println("--- TEST VERİLERİ YÜKLENDİ: " + seats.size() + " koltuk hazır ---");
        }
    }
}