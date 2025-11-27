package com.ticketguru.api_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/event")
    public Mono<String> eventServiceFallback() {
        return Mono.just("⚠️ Event Service şu an hizmet veremiyor. Lütfen daha sonra tekrar deneyiniz.");
    }

    @GetMapping("/payment")
    public Mono<String> paymentServiceFallback() {
        return Mono.just("⚠️ Ödeme sistemi geçici olarak devre dışı. Lütfen bekleyiniz.");
    }

    @GetMapping("/user")
    public Mono<String> userServiceFallback() {
        return Mono.just("⚠️ Kullanıcı işlemleri şu an yapılamıyor.");
    }
}