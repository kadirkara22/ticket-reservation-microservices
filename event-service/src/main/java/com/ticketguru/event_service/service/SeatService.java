package com.ticketguru.event_service.service;

import com.ticketguru.event_service.dto.EventSeatDto;
import com.ticketguru.event_service.dto.SeatReservedEvent;
import com.ticketguru.event_service.mapper.SeatMapper;
import com.ticketguru.event_service.model.EventSeat;
import com.ticketguru.event_service.model.SeatStatus;
import com.ticketguru.event_service.repository.EventSeatRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final EventSeatRepository seatRepository;
    private final SeatMapper seatMapper;
    private final RedisLockService redisLockService; // Redis servisini enjekte ettik
    private final KafkaProducerService kafkaProducerService;



    public List<EventSeatDto> getSeatsByEvent(Long eventId) {
        return seatRepository.findByEventId(eventId).stream()
                .map(seatMapper::toDto)
                .collect(Collectors.toList());
    }

    // Artık userId de alıyoruz ki kilidi kimin koyduğunu bilelim
    @Transactional
    public String reserveSeat(Long seatId, Long userId) {
        // 1. Önce koltuğun bilgilerini çekmemiz lazım (EventId ve SeatNumber için)
        // Not: Bu okuma işlemi hafiftir, lock gerektirmez.
        EventSeat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new RuntimeException("Koltuk bulunamadı!"));

        // 2. REDIS KİLİDİ (Distributed Lock)
        // Veritabanına yazmaya çalışmadan önce RAM'de kilit var mı bakıyoruz.
        boolean isLocked = redisLockService.lockSeat(seat.getEvent().getId(), seat.getSeatNumber(), userId);

        if (!isLocked) {
            throw new RuntimeException("Bu koltuk şu an başka bir kullanıcı tarafından işlem görüyor! (Redis Lock)");
        }

        // 3. VERİTABANI KONTROLÜ & GÜNCELLEME
        try {
            // Eğer koltuk zaten satılmışsa veya başkası kilitlemişse
            if (seat.getStatus() != SeatStatus.AVAILABLE) {
                throw new RuntimeException("Bu koltuk zaten dolu! Durum: " + seat.getStatus());
            }

            // Durumu LOCKED'a çekiyoruz (Ödeme bekleniyor)
            seat.setStatus(SeatStatus.LOCKED);
            seat.setLockTime(LocalDateTime.now());
            seatRepository.save(seat);

            SeatReservedEvent event = new SeatReservedEvent(
                    seat.getId(),
                    seat.getEvent().getId(),
                    userId,
                    LocalDateTime.now()
            );
            kafkaProducerService.sendSeatReservedEvent(event); // Kafka Producer'ı çağır

            return "Koltuk sizin için 10 dakikalığına ayrıldı! Ödeme ekranına yönlendiriliyorsunuz...";

        } catch (Exception e) {
            // Hata olursa (örn: DB patladı, Optimistic Lock hatası verdi vs.)
            // Redis kilidini boşa meşgul etmemek için kaldırmalıyız (Compensating Transaction)
            redisLockService.unlockSeat(seat.getEvent().getId(), seat.getSeatNumber());
            throw e; // Hatayı yukarı fırlat ki Controller yakalasın
        }
    }

    @Transactional
    public void releaseSeatForCompensation(Long eventSeatId) {
        EventSeat eventSeat = seatRepository.findById(eventSeatId)
                .orElseThrow(() -> new EntityNotFoundException("Etkinlik Koltu?u bulunamad?: " + eventSeatId));

        // Sadece REZERVE (RESERVED) durumundaysa serbest b?rak
        if (eventSeat.getStatus() == SeatStatus.LOCKED) {
            eventSeat.setStatus(SeatStatus.AVAILABLE);
            eventSeat.setLockTime(null); // LockTime'ı temizle
            seatRepository.save(eventSeat);
            log.warn("SAGA TELAFİ: EventSeat {} hata nedeniyle serbest b?rak?ld?.", eventSeatId);
        } else {
            log.info("EventSeat {} zaten RESERVED durumunda de?il, telafi gerekmiyor.", eventSeatId);
        }
    }
}