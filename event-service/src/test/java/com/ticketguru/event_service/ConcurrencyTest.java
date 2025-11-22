package com.ticketguru.event_service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrencyTest {

    public static void main(String[] args) throws InterruptedException {
        // Hedef: ID'si 1 olan koltuğu almaya çalışacağız
        // NOT: Testi çalıştırmadan önce veritabanını sıfırla veya uygulamanı yeniden başlat ki koltuk "AVAILABLE" olsun.
        String url = "http://localhost:8081/api/events/seats/8/reserve";

        int numberOfThreads = 20; // Aynı anda saldıracak kullanıcı sayısı
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        System.out.println("--- SALDIRI BAŞLIYOR: " + numberOfThreads + " kişi aynı koltuğa saldırıyor! ---");

        for (int i = 0; i < numberOfThreads; i++) {
            int userId = i + 1;
            executor.submit(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(url + "?userId=" + userId))
                            .POST(HttpRequest.BodyPublishers.noBody()) // POST isteği
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    // Sonuçları yazdır
                    if (response.statusCode() == 200) {
                        System.out.println("Kullanıcı " + userId + " -> BAŞARILI! " + response.body());
                    } else {
                        System.out.println("Kullanıcı " + userId + " -> BAŞARISIZ. Kod: " + response.statusCode() + " Mesaj: " + response.body());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("--- TEST BİTTİ ---");
    }
}