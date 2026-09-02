# Current State — MarketCanvas

> **Last updated:** 2026-08-15
> **Updated by:** Senior Staff Engineer

## Current Branch

`main` (single branch, no branching strategy established yet)

## Architecture Stage

**Stage 2 In Progress** — Event-Driven Modular Monolith (Backend CRUD & Asset Registry complete, building Next.js Frontend)

## Implemented Features

### ✅ Fully Complete

| Component | Status | Notes |
|-----------|--------|-------|
| Package-by-domain structure | ✅ | 4 bounded contexts: `user`, `watchlist`, `marketdata`, `sharedkernel` |
| Shared Kernel value objects | ✅ | `UserId`, `AssetId` as Java records with null validation |
| Watchlist Aggregate Root | ✅ | JPA Entity. Factory method, business invariants (max 10 assets, name validation), domain events |
| Watchlist JPA Entity | ✅ | `@Entity`, `@ElementCollection` for assets, `AttributeConverter`s for value objects |
| Watchlist REST API | ✅ | Full CRUD: `POST /api/v1/watchlists`, `GET /api/v1/watchlists?ownerId=`, `GET /api/v1/watchlists/{id}`, `DELETE /api/v1/watchlists/{id}`, `POST /api/v1/watchlists/{id}/assets`, `DELETE /api/v1/watchlists/{id}/assets/{assetId}` |
| Watchlist Application Service | ✅ | `WatchlistService` orchestrates use cases with `@Transactional` / `@Transactional(readOnly=true)` |
| WatchlistResponse DTO | ✅ | `WatchlistResponse` record decouples domain aggregate from REST wire format |
| Asset Registry Service | ✅ | Loads 50 real US stocks from classpath `data/assets.json` with deterministic UUIDs |
| Asset REST API | ✅ | `GET /api/v1/assets`, `GET /api/v1/assets/{id}`, `GET /api/v1/assets/search?q=` |
| Mock User REST API | ✅ | `GET /api/v1/users/mock` in `user/infrastructure` |
| Next.js Frontend Dashboard | ✅ | Next.js 14+ App Router, Tailwind dark terminal theme, User Switcher (Alice, Bob, Carol) |
| Watchlist Management UI | ✅ | Dashboard grid, Create Watchlist modal, Watchlist detail view with capacity bar (10 max) |
| Asset Search & Directory UI | ✅ | Debounced search across 50 US stocks, Sector filters, "+ Add to Watchlist" action |
| Interactive REST API Test Bench | ✅ | In-app drawer with 1-click test suite for all 10 endpoints, latency, and JSON payload viewer |
| Multi-Stage Containerization | ✅ | Root Spring Boot `Dockerfile` + Next.js standalone `frontend/Dockerfile` |
| Unified Docker Compose | ✅ | PostgreSQL (5433), Kafka KRaft (9092), Spring Boot Backend (8080), Next.js Frontend (3000) |
| ArchUnit enforcement tests | ✅ | Scoped to application packages; cross-module dependency violations fail test |
| Outbox Event entity | ✅ | JPA entity with Lombok, `processed` flag, timestamps |
| Outbox Event Repository | ✅ | `findTop100ByProcessedFalseOrderByCreatedAt()` |
| Outbox Relay (Publisher) | ✅ | `@Scheduled(fixedDelay=5000)`, blocking `.get()` on Kafka send, envelope wrapping |
| WatchlistOutboxListener | ✅ | `@TransactionalEventListener(BEFORE_COMMIT)`, serializes to Outbox |
| Kafka Consumer | ✅ | Idempotent via `ProcessedEvent` table, envelope parsing |
| Application config | ✅ | `application.yml` with JPA, Kafka, `open-in-view: false` |
| `@EnableScheduling` | ✅ | Added to `MarketCanvasApplication` |
| SecurityConfig & CORS | ✅ | Permits `/api/**`, global CORS enabled for `http://localhost:3000` |
| JPA AttributeConverters | ✅ | `UserIdConverter`, `AssetIdConverter` with `autoApply = true` |
| Kafka Error Handling & DLQ | ✅ | `DefaultErrorHandler`, fixed backoff, direct routing to DLQ for `JacksonException` |
| Topic Constants | ✅ | Centralized `KafkaTopics.java` used across producers and consumers |
| Market Data Providers | ✅ | `FinnhubMarketDataProvider` + `YahooFinanceMarketDataProvider` with `@CircuitBreaker` and `@RateLimiter` |
| Market Data Persistence | ✅ | `AssetQuoteEntity` & `AssetQuoteRepository` for PostgreSQL persistence |
| Multi-Tier Cache Engine | ✅ | `MultiTierMarketDataCache` (L1 ConcurrentHashMap 90s, L2 Redis 5m) + graceful degradation |
| Market Data Polling | ✅ | `AdaptiveMarketDataScheduler` polling 10 tickers per batch during NYSE hours |
| SSE Broadcasting | ✅ | `MarketDataBroadcaster` & `GET /api/v1/assets/stream` endpoint |

## Work In Progress

| Item | Status | Notes |
|------|--------|-------|
| TASK-017: Real Market Data Ingestion & Snapshot Producer | 🔄 Near Completion | Backend pipeline, multi-tier cache, and SSE broadcasting complete. Frontend SSE integration remaining. |

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
| 2026-08-15 | Market Data Ingestion Pipeline (TASK-017), Multi-tier Cache, Redis, SSE Broadcasting |

## Infrastructure Notes

- **PostgreSQL port:** Docker maps to `5433` (not `5432`) because a native Windows PostgreSQL installation occupies `5432`.
- **Kafka dependency:** Must use `spring-boot-starter-kafka` (not raw `spring-kafka`) in Spring Boot 4.x for auto-configuration.
- **PowerShell cURL:** Use `curl.exe` (not `curl`) on Windows PowerShell. Escape JSON with `\"`.
- **Redis dependency:** Port 16379 mapped to 6379 in Docker. `spring-boot-starter-data-redis` requires `jackson-datatype-jsr310` for `JavaTimeModule` support during serialization.
