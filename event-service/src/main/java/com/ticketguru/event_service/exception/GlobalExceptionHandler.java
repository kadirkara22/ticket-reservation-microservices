package com.ticketguru.event_service.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Optimistic Locking hatası (Yarış durumu) olduğunda buraya düşer
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<String> handleOptimisticLocking(OptimisticLockingFailureException ex) {
        // 500 yerine 409 (Conflict) dönüyoruz.
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Üzgünüz, seçtiğiniz koltuk az önce başkası tarafından satın alındı. Lütfen sayfayı yenileyip başka bir koltuk seçin.");
    }

    // Genel Runtime hatalar (Örn: Koltuk zaten satılmışsa)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}