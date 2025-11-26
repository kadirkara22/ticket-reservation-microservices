package com.ticketguru.payment_service.service;

import com.ticketguru.payment_service.model.PaymentTransaction;
import com.ticketguru.payment_service.repository.PaymentTransactionRepository;
// DİKKAT: Senin paketin payment_service (alt çizgi var mı kontrol et)
import com.ticketguru.payment_service.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;

    // KafkaTemplate yerine senin yazdığın özel servisi çağırıyoruz
    private final KafkaProducerService kafkaProducerService;

    public PaymentTransaction processPayment(PaymentTransaction payment) {
        log.info("Ödeme işlemi başladı: {}", payment.getAmount());

        // 1. Veritabanına kaydet
        PaymentTransaction savedPayment = paymentRepository.save(payment);

        // 2. Senin servisin üzerinden Kafka'ya mesaj at
        // Bu metod arka planda JSON'a çevirip yollayacak.
        kafkaProducerService.sendPaymentSuccessfulEvent(savedPayment);

        log.info("KafkaProducerService tetiklendi. Ödeme ID: {}", savedPayment.getId());

        return savedPayment;
    }
}