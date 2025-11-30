# 🎟️ Cloud-Native Ticket Booking System

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.0-green?style=for-the-badge&logo=spring)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-blue?style=for-the-badge&logo=kubernetes)
![Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-red?style=for-the-badge&logo=apachekafka)
![License](https://img.shields.io/badge/License-MIT-lightgrey?style=for-the-badge)

A highly scalable, **Event-Driven Microservices** application built with **Spring Boot 3** and **Kubernetes**.
This project demonstrates advanced patterns like **SAGA (Choreography)**, **Distributed Locking (Redis)**, **Resilience (Circuit Breaker)**, and **Observability**.

---

## 🏛️ Architecture Overview

The system follows a microservices architecture where each service has its own database (**Database per Service** pattern) and communicates asynchronously via **Kafka**.

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
```


🚀 Key Features & Patterns
Feature	Technology / Pattern	Description
Service Discovery	Netflix Eureka	Dynamic service registration and discovery.
API Gateway	Spring Cloud Gateway	Centralized entry point, routing, and rate limiting.
Authentication	JWT & Spring Security	Stateless security mechanism protecting internal services.
Event-Driven	Apache Kafka	Asynchronous communication between services.
Concurrency Control	Redis Distributed Lock	Prevents double-booking of the same seat.
Resilience	Resilience4j	Circuit Breaker pattern to handle service failures gracefully.
Data Consistency	SAGA Pattern	Handles distributed transactions (Compensating transactions).
Observability	Prometheus & Grafana	Real-time metrics monitoring and visualization.
Tracing	Zipkin & Micrometer	Distributed tracing to debug latency and flow issues.
Containerization	Docker	Fully containerized environment for all services.
Orchestration	Kubernetes (Minikube)	Deployment, scaling, and management of containers.
CI/CD	GitHub Actions	Automated build, test, and deploy pipeline to Docker Hub.