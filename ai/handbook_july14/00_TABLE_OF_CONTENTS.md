# MarketCanvas — The Definitive Engineering Handbook

> **Version:** 1.0  
> **Date:** 2026-07-14  
> **Audience:** New engineers with basic Java knowledge  
> **Purpose:** After reading this document, you will understand every package, every class, every method, every line of important code, and every architectural decision in the MarketCanvas codebase.

---

## How to Read This Handbook

This handbook is split into **10 chapter files** due to its size. Read them in order:

| # | Chapter File | Topics Covered |
|---|-------------|----------------|
| 1 | [Chapter 1: Vision & Foundations](./01_VISION_AND_FOUNDATIONS.md) | Project vision, business context, technology stack, Maven build system, why each technology was chosen |
| 2 | [Chapter 2: Spring Boot From First Principles](./02_SPRING_BOOT_FUNDAMENTALS.md) | IoC, DI, auto-configuration, beans, application context, component scan, lifecycle, annotations — all connected to this project |
| 3 | [Chapter 3: Domain-Driven Design](./03_DOMAIN_DRIVEN_DESIGN.md) | DDD history, strategic/tactical design, bounded contexts, aggregates, value objects, entities, domain events — mapped to every class |
| 4 | [Chapter 4: The Architecture](./04_ARCHITECTURE.md) | Modular monolith, hexagonal architecture, package structure, module boundaries, ArchUnit enforcement, dependency graphs |
| 5 | [Chapter 5: The Domain Model — Complete Code Walkthrough](./05_DOMAIN_MODEL_WALKTHROUGH.md) | Line-by-line walkthrough of every class: Watchlist, UserId, AssetId, OutboxEvent, WatchlistService, WatchlistController |
| 6 | [Chapter 6: Event-Driven Architecture & Apache Kafka](./06_EVENT_DRIVEN_KAFKA.md) | Kafka from first principles, the Outbox Pattern, producers, consumers, idempotency, poison pills, DLQ, complete event flow |
| 7 | [Chapter 7: PostgreSQL & Data Persistence](./07_DATABASE_AND_PERSISTENCE.md) | PostgreSQL internals, JPA/Hibernate, ACID, transactions, schema design, ER diagrams, AttributeConverters, repositories |
| 8 | [Chapter 8: REST API & Spring Security](./08_REST_API_AND_SECURITY.md) | REST from first principles, HTTP lifecycle, controllers, request flow, Spring Security filter chain, authentication/authorization |
| 9 | [Chapter 9: Testing & Quality](./09_TESTING_AND_QUALITY.md) | Unit tests, ArchUnit, integration tests, test patterns, every test class explained |
| 10 | [Chapter 10: DevOps, Deployment & Production Readiness](./10_DEVOPS_AND_PRODUCTION.md) | Docker, Docker Compose, CI/CD, monitoring, performance, security hardening, project evolution roadmap |

---

## Quick Reference: Complete File Inventory

Every source file in the project, with its location and purpose:

| # | File | Package | Purpose |
|---|------|---------|---------|
| 1 | `MarketCanvasApplication.java` | root | Application entry point |
| 2 | `SecurityConfig.java` | root | Spring Security configuration |
| 3 | `UserId.java` | sharedkernel.domain | Value Object — user identity |
| 4 | `AssetId.java` | sharedkernel.domain | Value Object — asset identity |
| 5 | `OutboxEvent.java` | sharedkernel.domain | JPA Entity — outbox table row |
| 6 | `WatchlistItemAddedEvent.java` | sharedkernel.events | Domain Event record |
| 7 | `OutboxEventRepository.java` | sharedkernel.infrastructure | JPA Repository for outbox |
| 8 | `OutboxRelay.java` | sharedkernel.infrastructure | Scheduled Kafka publisher |
| 9 | `WatchlistOutboxListener.java` | sharedkernel.infrastructure | Transactional event listener |
| 10 | `UserIdConverter.java` | sharedkernel.infrastructure | JPA AttributeConverter |
| 11 | `AssetIdConverter.java` | sharedkernel.infrastructure | JPA AttributeConverter |
| 12 | `Watchlist.java` | watchlist.domain | Aggregate Root — JPA Entity |
| 13 | `WatchlistService.java` | watchlist.application | Application Service |
| 14 | `WatchlistController.java` | watchlist.infrastructure | REST Controller |
| 15 | `WatchlistRepository.java` | watchlist.infrastructure | JPA Repository |
| 16 | `CreateWatchlistRequest.java` | watchlist.infrastructure | Request DTO |
| 17 | `AddAssetRequest.java` | watchlist.infrastructure | Request DTO |
| 18 | `WatchlistEventConsumer.java` | marketdata.application.messaging | Kafka Consumer |
| 19 | `ProcessedEvent.java` | marketdata.infrastructure.messaging | Idempotency entity |
| 20 | `ProcessedEventRepository.java` | marketdata.infrastructure.messaging | JPA Repository |

### Test Files

| # | File | Purpose |
|---|------|---------|
| 1 | `MarketCanvasApplicationTests.java` | Spring context load test |
| 2 | `WatchlistTest.java` | Watchlist aggregate unit test |
| 3 | `ArchitectureEnforcementTest.java` | ArchUnit boundary enforcement |

### Configuration Files

| # | File | Purpose |
|---|------|---------|
| 1 | `pom.xml` | Maven build configuration |
| 2 | `application.yml` | Spring Boot configuration |
| 3 | `docker-compose.yml` | Local infrastructure |
