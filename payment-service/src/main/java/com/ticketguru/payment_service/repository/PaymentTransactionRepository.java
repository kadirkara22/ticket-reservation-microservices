package com.ticketguru.payment_service.repository;

import com.ticketguru.payment_service.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {


    boolean existsByEventIdAndSeatId(String eventId, String seatId);

}