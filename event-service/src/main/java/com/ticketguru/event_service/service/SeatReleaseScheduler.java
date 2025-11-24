    package com.ticketguru.event_service.service;


    import com.ticketguru.event_service.model.EventSeat;
    import com.ticketguru.event_service.model.SeatStatus;
    import com.ticketguru.event_service.repository.EventSeatRepository;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.scheduling.annotation.Scheduled;
    import org.springframework.stereotype.Component;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class SeatReleaseScheduler {

        private final EventSeatRepository seatRepository;
        private final RedisLockService redisLockService;

        // Her 1 dakikada bir çalış (60.000 ms)
        @Scheduled(fixedRate = 60000)
        @Transactional
        public void releaseExpiredSeats() {
            LocalDateTime tenMinutesAgo = LocalDateTime.now().minusSeconds(30);

            // Test için 10 dakika beklemeyelim, 10 saniye yapalım ki sonucu hemen gör:
            // LocalDateTime tenMinutesAgo = LocalDateTime.now().minusSeconds(10);

            log.info("Süresi dolmuş rezervasyonlar kontrol ediliyor: {} tarihinden öncekiler...", tenMinutesAgo);

            // 1. Süresi dolmuş koltukları bul
            List<EventSeat> expiredSeats = seatRepository.findByStatusAndLockTimeBefore(SeatStatus.LOCKED, tenMinutesAgo);

            if (expiredSeats.isEmpty()) {
                log.info("Süresi dolmuş koltuk bulunamadı.");
                return;
            }

            // 2. Hepsini serbest bırak
            for (EventSeat seat : expiredSeats) {
                log.info("Koltuk serbest bırakılıyor: {} (ID: {})", seat.getSeatNumber(), seat.getId());

                // DB'den düzelt
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setLockTime(null);

                // Redis'ten de kilidi kaldır (Garanti olsun diye)
                redisLockService.unlockSeat(seat.getEvent().getId(), seat.getSeatNumber());
            }

            // Toplu kaydetme (Batch Update)
            seatRepository.saveAll(expiredSeats);
            log.info("Toplam {} adet koltuk tekrar satışa açıldı.", expiredSeats.size());
        }
    }