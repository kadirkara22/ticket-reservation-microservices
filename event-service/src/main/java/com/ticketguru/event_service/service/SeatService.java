    package com.ticketguru.event_service.service;

    import com.ticketguru.event_service.client.UserServiceClient;
    import com.ticketguru.event_service.dto.EventSeatDto;
    import com.ticketguru.event_service.event.SeatReservedEvent;
    import com.ticketguru.event_service.mapper.SeatMapper;
    import com.ticketguru.event_service.model.EventSeat;
    import com.ticketguru.event_service.model.SeatStatus;
    import com.ticketguru.event_service.repository.EventSeatRepository;
    import jakarta.persistence.EntityNotFoundException;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.transaction.support.TransactionSynchronization;
    import org.springframework.transaction.support.TransactionSynchronizationManager;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.stream.Collectors;

    @Service
    @RequiredArgsConstructor
    @Slf4j
    public class SeatService {

        private final EventSeatRepository seatRepository;
        private final SeatMapper seatMapper;
        private final RedisLockService redisLockService;
        private final KafkaProducerService kafkaProducerService;
        private final UserServiceClient userServiceClient;



        public List<EventSeatDto> getSeatsByEvent(Long eventId) {
            // Eski metot yerine "WithFetch" olanı çağırıyoruz
            return seatRepository.findByEventIdWithFetch(eventId).stream()
                    .map(seatMapper::toDto)
                    .collect(Collectors.toList());
        }

        @Transactional
        public String reserveSeat(Long eventSeatDatabaseId, Long userId) {
            log.info("📢 HTTP İsteği Geldi! Trace ID Kontrolü yapılıyor...");
            try {
                userServiceClient.validateUser(userId);
            } catch (Exception e) {
                throw new RuntimeException("Geçersiz Kullanıcı ID! Böyle bir kullanıcı sistemde kayıtlı değil.");
            }

            // 1. Koltuğu Bul
            EventSeat seat = seatRepository.findById(eventSeatDatabaseId)
                    .orElseThrow(() -> new RuntimeException("Koltuk bulunamadı! ID: " + eventSeatDatabaseId));

            // 2. Redis Kilidi (Atomik)
            boolean isLocked = redisLockService.lockSeat(seat.getEvent().getId(), seat.getSeatNumber(), userId);
            if (!isLocked) {
                throw new RuntimeException("Bu koltuk şu an başka bir kullanıcı tarafından işlem görüyor!");
            }

            try {
                // 3. Veritabanı Kontrolü & Güncelleme
                if (seat.getStatus() != SeatStatus.AVAILABLE) {
                    throw new RuntimeException("Bu koltuk zaten dolu! Durum: " + seat.getStatus());
                }

                seat.setStatus(SeatStatus.LOCKED);
                seat.setLockTime(LocalDateTime.now());
                seat.setUserId(userId);

                // DB'ye flush edilir ama henüz commit edilmedi
                seatRepository.save(seat);

                // 4. KAFKA EVENT (Transaction Commit Sonrası Çalışacak)
                // Bu event nesnesini hazırlıyoruz
                SeatReservedEvent event = SeatReservedEvent.builder()
                        .eventId(seat.getEvent().getId())
                        .seatNumber(seat.getSeatNumber())
                        .userId(userId)
                        .reservationTime(LocalDateTime.now())
                        .build();

                // SENIOR DOKUNUŞU BURASI:
                // "Veritabanı transaction'ı başarılı olursa bu kodu çalıştır" diyoruz.
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        log.info("Transaction commit edildi. Kafka mesajı gönderiliyor...");
                        kafkaProducerService.sendSeatReservedEvent(event);
                    }
                });

                return "Koltuk ayrıldı, ödeme bekleniyor...";

            } catch (Exception e) {
                // Hata olursa Redis kilidini hemen kaldır ki başkası alabilsin
                redisLockService.unlockSeat(seat.getEvent().getId(), seat.getSeatNumber());
                throw e;
            }
        }

        @Transactional
        public void releaseSeatForCompensation(Long eventSeatId) {
            EventSeat eventSeat = seatRepository.findById(eventSeatId)
                    .orElseThrow(() -> new EntityNotFoundException("Etkinlik Koltuğu bulunamadı: " + eventSeatId));

            // Sadece Kilitli durumunda ise durumundaysa serbest bırak
            if (eventSeat.getStatus() == SeatStatus.LOCKED) {
                eventSeat.setStatus(SeatStatus.AVAILABLE);
                eventSeat.setLockTime(null);
                seatRepository.save(eventSeat);
                log.warn("SAGA TELAFİ: EventSeat {} hata nedeniyle serbest bırakıldı.", eventSeatId);
            } else {
                log.info("EventSeat {} zaten LOCK durumunda değil, telafi gerekmiyor.", eventSeatId);
            }
        }

        @Transactional
        public void markSeatAsPaid(String eventIdStr, String seatNumberStr, Long userId) {
            log.info("Ödeme başarılı oldu. Koltuk 'SOLD' olarak güncelleniyor: EventId={}, SeatNumber={}, UserId={}", eventIdStr, seatNumberStr, userId);

            Long actualEventId = Long.valueOf(eventIdStr);

            // Koltu?u EventId ve EventSeat'in seatNumber'ı ile bul
            EventSeat eventSeat = seatRepository.findByEventIdAndSeatNumber(actualEventId, seatNumberStr)
                    .orElseThrow(() -> new EntityNotFoundException("Koltuk bulunamadı: EventId=" + eventIdStr + ", SeatNumber=" + seatNumberStr));

            // Eğer koltuk LOCKED durumundaysa güncelle
            if (eventSeat.getStatus() == SeatStatus.LOCKED) {
                eventSeat.setStatus(SeatStatus.SOLD);
                eventSeat.setLockTime(null);
                eventSeat.setUserId(userId);
                seatRepository.save(eventSeat);
                log.info("Koltuk (EventId={}, SeatNumber={}) başarıyla 'SOLD' olarak güncellendi.", eventIdStr, seatNumberStr);
            } else {
                log.warn("Koltuk (EventId={}, SeatNumber={}) beklenenden farklı bir durumda ({}) olduğu için güncellenmedi. Belki zaten satılmıştır.",
                        eventIdStr, seatNumberStr, eventSeat.getStatus());
            }

            // REDIS KİLİDİNİ KALDIR

            redisLockService.unlockSeat(actualEventId, seatNumberStr);
            log.info("Redis kilidi (EventId={}, SeatNumber={}) serbest bırakıldı.", eventIdStr, seatNumberStr);
        }

        // com.ticketguru.event_service.service.SeatService
        @Transactional
        public void markSeatAsAvailable(String eventIdStr, String seatNumberStr) {
            Long actualEventId = Long.valueOf(eventIdStr);
            EventSeat eventSeat = seatRepository.findByEventIdAndSeatNumber(actualEventId, seatNumberStr)
                    .orElseThrow(() -> new EntityNotFoundException("Koltuk bulunamadı! EventId=" + eventIdStr + ", SeatNumber=" + seatNumberStr));

            if (eventSeat.getStatus() == SeatStatus.LOCKED) { // Sadece kilitliyse bo?a ç?kar
                eventSeat.setStatus(SeatStatus.AVAILABLE);
                eventSeat.setLockTime(null);
                eventSeat.setUserId(null);
                seatRepository.save(eventSeat);
                log.info("Koltuk (EventId={}, SeatNumber={}) başarısız ödeme sonrası 'AVAILABLE' olarak güncellendi.", eventIdStr, seatNumberStr);
            } else {
                log.warn("Koltuk (EventId={}, SeatNumber={}) LOCKED durumunda değil, boşa çıkarılmad?. Mevcut durum: {}",
                        eventIdStr, seatNumberStr, eventSeat.getStatus());
            }
            redisLockService.unlockSeat(actualEventId, seatNumberStr);
            log.info("Redis kilidi (EventId={}, SeatNumber={}) başarısız ödeme sonrası serbest bırakıldı.", eventIdStr, seatNumberStr);
        }
    }