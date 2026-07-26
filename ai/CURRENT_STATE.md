# Current State — MarketCanvas

> **Last updated:** 2026-07-25
> **Updated by:** Senior Staff Engineer (AI Review Session)

## Current Branch

`main` (single branch, no branching strategy established yet)

## Architecture Stage

**Stage 2 In Progress** — Event-Driven Modular Monolith (Backend complete, building Full-Stack MVP)

## Implemented Features

### ✅ Fully Complete

| Component | Status | Notes |
|-----------|--------|-------|
| Package-by-domain structure | ✅ | 4 bounded contexts: `user`, `watchlist`, `marketdata`, `sharedkernel` |
| Shared Kernel value objects | ✅ | `UserId`, `AssetId` as Java records with null validation |
| Watchlist Aggregate Root | ✅ | JPA Entity. Factory method, business invariants (max 10 assets, name validation), domain events |
| Watchlist JPA Entity | ✅ | `@Entity`, `@ElementCollection` for assets, `AttributeConverter`s for value objects |
| Watchlist REST API | ✅ | `POST /api/v1/watchlists`, `POST /api/v1/watchlists/{id}/assets` |
| Watchlist Application Service | ✅ | `WatchlistService` orchestrates use cases with `@Transactional` |
| ArchUnit enforcement tests | ✅ | Cross-module dependency violations cause test failure |
| Outbox Event entity | ✅ | JPA entity with Lombok, `processed` flag, timestamps |
| Outbox Event Repository | ✅ | `findTop100ByProcessedFalseOrderByCreatedAt()` |
| Outbox Relay (Publisher) | ✅ | `@Scheduled(fixedDelay=5000)`, blocking `.get()` on Kafka send, envelope wrapping |
| WatchlistOutboxListener | ✅ | `@TransactionalEventListener(BEFORE_COMMIT)`, serializes to Outbox |
| Kafka Consumer | ✅ | Idempotent via `ProcessedEvent` table, two-tier exception handling, envelope parsing |
| Docker Compose | ✅ | Kafka (KRaft) + PostgreSQL 16 with named volume. DB on port **5433** (avoids native PG conflict) |
| Application config | ✅ | `application.yml` with JPA, Kafka, `open-in-view: false` |
| `@EnableScheduling` | ✅ | Added to `MarketCanvasApplication` |
| SecurityConfig | ✅ | Permits `/api/**`, CSRF disabled for REST. Development-only config. |
| JPA AttributeConverters | ✅ | `UserIdConverter`, `AssetIdConverter` with `autoApply = true` |
| Kafka Error Handling & DLQ | ✅ | `DefaultErrorHandler`, fixed backoff, direct routing to DLQ for `JacksonException` |
| Topic Constants | ✅ | Centralized `KafkaTopics.java` used across producers and consumers |

## Work In Progress

| Item | Status | Notes |
|------|--------|-------|
| MVP Sprint: Full-Stack Watchlist App | 🔄 Planning | TASK-020 (Asset Registry), TASK-021 (CRUD API), TASK-022 (Next.js Frontend) |

## Known Bugs

| # | Bug | Severity | File |
|---|-----|----------|------|
| (No known bugs) | | | |

## Pending Refactoring

| Item | Status | Notes |
|------|--------|-------|
| Event Schema Versioning | Todo | Need to design strategy for evolving event schemas safely |

| Item | Impact | Effort to Fix |
|------|--------|---------------|
| `hibernate.ddl-auto: update` in prod would be dangerous | Must switch to Flyway migrations before production | Low now, Critical later |
| `processed_events` table grows unbounded | Needs a cleanup/retention job | Low |
| No test for Kafka consumer | Consumer logic is untested | Medium |
| SecurityConfig permits all `/api/**` | Needs proper auth before production | Medium |

## Recently Completed Work

| Date | Work |
|------|------|
| 2026-06-22 | Modular monolith structure, value objects, ArchUnit tests |
| 2026-06-26 | Outbox Pattern, Kafka infrastructure, PostgreSQL setup |
| 2026-07-05 | Kafka consumer with idempotency, `@EnableScheduling` fix, exception handling |
| 2026-07-10 | Bug fixes: payload schema mismatch, WatchlistTest, deleted legacy listener, AssetId→UUID in events, merged config files |
| 2026-07-12 | JPA Entity mapping, REST API, AttributeConverters, SecurityConfig, port fix (5432→5433), starter-kafka dependency, **End-to-End test PASSED** |
| 2026-07-25 | Built Kafka Dead Letter Queue (DLQ), centralized `KafkaTopics` constants, implemented `DefaultErrorHandler` |

## Infrastructure Notes

- **PostgreSQL port:** Docker maps to `5433` (not `5432`) because a native Windows PostgreSQL installation occupies `5432`.
- **Kafka dependency:** Must use `spring-boot-starter-kafka` (not raw `spring-kafka`) in Spring Boot 4.x for auto-configuration.
- **PowerShell cURL:** Use `curl.exe` (not `curl`) on Windows PowerShell. Escape JSON with `\"`.
