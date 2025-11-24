package com.ticketguru.event_service.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;


    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    private BigDecimal price;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    // --- EXPERT BÖLÜMÜ: OPTIMISTIC LOCKING ---
    // Bu alan sayesinde aynı anda 2 kişi bu koltuğu alamaz.
    // İlk güncelleyen kazanır, diğeri hata alır.
    @Version
    private Long version;


    @Override
    public String toString() {
        return "EventSeat{" +
                "id=" + id +
                ", seatNumber='" + seatNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}