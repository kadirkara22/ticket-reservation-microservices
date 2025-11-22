package com.ticketguru.event_service.model;

public enum SeatStatus {
    AVAILABLE,  // Satışa açık
    LOCKED,     // Biri sepete attı, ödeme yapıyor (Geçici kilit)
    SOLD        // Satıldı
}
