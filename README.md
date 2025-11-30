# 🎟️ Cloud-Native Ticket Booking System

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-green?style=for-the-badge&logo=spring)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-blue?style=for-the-badge&logo=kubernetes)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-red?style=for-the-badge&logo=apachekafka)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=for-the-badge)

A highly scalable, **Event-Driven Microservices** application built with **Spring Boot 3** and **Kubernetes**.

---

## 🏛️ Architecture Overview

```mermaid
graph TD
    Client[Client / Postman] -->|HTTP| Gateway[API Gateway]
    Gateway -->|Routing & Security| UserSvc[User Service]
    Gateway -->|Routing & Circuit Breaker| EventSvc[Event Service]
    Gateway -->|Routing| PaymentSvc[Payment Service]
    
    UserSvc -->|Auth & Validation| DB_User[(PostgreSQL User DB)]
    
    EventSvc -->|Lock Seat| Redis[(Redis Cache)]
    EventSvc -->|Persist Data| DB_Event[(PostgreSQL Event DB)]
    EventSvc -->|Produce Event| Kafka{Apache Kafka}
    
    Kafka -->|Consume Event| PaymentSvc
    PaymentSvc -->|Persist Tx| DB_Payment[(PostgreSQL Payment DB)]
    PaymentSvc -->|Payment Result| Kafka
    
    Kafka -->|Consume Result| NotifSvc[Notification Service]
    NotifSvc -->|Send Email| Gmail[SMTP Server]
    
    subgraph Observability
        Prometheus -->|Scrape Metrics| Gateway & EventSvc & PaymentSvc
        Grafana -->|Visualize| Prometheus
        Zipkin -->|Distributed Tracing| Gateway & EventSvc
    end