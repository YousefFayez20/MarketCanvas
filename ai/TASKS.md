# Task Board — MarketCanvas

> Living task board. Update after every session.  
> **Last updated:** 2026-08-15

---

## 🔴 Blocked

*None currently.*

---

## 🟡 In Progress

*None currently.*

---

## 📋 Todo

### TASK-017: Real Market Data Ingestion & Snapshot Producer (Kafka)
- **Priority:** 🔴 HIGH
- **Dependencies:** None (MVP Sprint gate completed)
- **Description:** Implement a real market data ingestion pipeline with pluggable providers (Finnhub.io primary with 60 req/min free tier + Yahoo Finance public fallback) and a daily snapshot scheduler (2-3 daily snapshots: Market Open 09:35, Midday 13:00, Market Close 16:05 EST + on-demand manual refresh trigger).
- **Subtasks:**
  - [x] Implement `MarketDataProvider` port interface (`fetchQuote`, `fetchBatchQuotes`).
  - [x] Build `FinnhubMarketDataProvider` adapter using Spring `RestClient` with safe rate-limiting.
  - [x] Build `YahooFinanceMarketDataProvider` zero-config fallback adapter.
  - [x] Create `AssetQuoteEntity` and `AssetQuoteRepository` for PostgreSQL real-time snapshot caching.
  - [x] Build `ResilientMarketDataService` orchestrator with concurrent in-memory caching.
  - [x] Create `StockPriceUpdatedEvent` in `sharedkernel` domain events.
  - [x] Build `MarketDataSnapshotScheduler` (`@Scheduled` cron snapshots + dev interval).
  - [x] Publish `StockPriceUpdatedEvent` messages to Kafka topic `platform.marketdata.prices`.
  - [x] Create `MarketDataController` REST endpoints (`GET /api/v1/marketdata/quotes/{ticker}`, `GET /api/v1/marketdata/quotes`, `POST /api/v1/marketdata/refresh`).
  - [ ] Connect Next.js frontend to display real stock prices, 24h delta %, and manual sync button. (Frontend SSE integration remaining)

### TASK-018: Event Schema Design (Avro / JSON Schema)
- **Priority:** 🟡 MEDIUM
- **Dependencies:** TASK-017
- **Notes:** Introduce Confluent Schema Registry to docker-compose and migrate `StringSerializer` to `KafkaAvroSerializer` or `KafkaJsonSchemaSerializer` for `WatchlistItemAddedEvent` and `StockPriceUpdatedEvent`.

### TASK-019: Kafka Connect to S3 / MinIO (Event Archive)
- **Priority:** 🟡 MEDIUM
- **Dependencies:** TASK-017
- **Notes:** Set up Kafka Connect in docker-compose with an S3 sink connector (MinIO locally) to archive all domain events and price snapshots for cold storage and AI RAG analysis.

---

## ✅ Completed

### TASK-022: Next.js Frontend & Full-Stack Docker Orchestration
- **Completed:** 2026-08-01 (Session 7)
- **Notes:** Built Next.js 14+ App Router dashboard with dark Bloomberg/fintech theme, User Switcher (Alice, Bob, Carol), Watchlist Manager with CRUD and capacity enforcement (10 max), real Asset Search & Directory across 50 US equities, live financial sparklines, interactive REST API Test Bench (testing all 10 endpoints), multi-stage Dockerfiles for backend & frontend, and updated `docker-compose.yml` for unified 4-service container orchestration.

### TASK-020: Asset Registry + Mock Users (Backend)
- **Completed:** 2026-08-01 (Session 6)
- **Notes:** Created `AssetRegistry` service with 50 real US stocks from a static JSON file. Exposed via `GET /api/v1/assets/search?q=`, `GET /api/v1/assets/{id}`, and `GET /api/v1/assets`. Created `MockUserController` with 3 demo users at `GET /api/v1/users/mock`.

### TASK-021: Complete Watchlist CRUD API (Backend)
- **Completed:** 2026-08-01 (Session 6)
- **Notes:** Added GET (list by owner, get by id), DELETE (watchlist, asset) endpoints. Added `WatchlistResponse` DTO. Added global CORS for localhost:3000 in `SecurityConfig`. Added `findByOwnerId` to `WatchlistRepository`.

### TASK-015: Dead Letter Queue (DLQ)
- **Completed:** 2026-07-25
- **Notes:** Configured `DefaultErrorHandler` with 3 retries (1s backoff) and `DeadLetterPublishingRecoverer`. Added non-retryable exceptions for Jackson, UUID, and Domain violations.

### TASK-016: Kafka Topic Constants
- **Completed:** 2026-07-25
- **Notes:** Created `KafkaTopics` and updated `OutboxRelay` and `WatchlistEventConsumer` to use it.

### TASK-001: Modular Monolith Package Structure
- **Completed:** 2026-06-22
- **Notes:** 4 bounded contexts established with ArchUnit enforcement.

### TASK-002: Shared Kernel Value Objects
- **Completed:** 2026-06-22
- **Notes:** `UserId`, `AssetId` as Java records with null validation.

### TASK-003: Watchlist Aggregate Root
- **Completed:** 2026-06-22
- **Notes:** Factory method, max 10 assets invariant, domain event queuing.

### TASK-004: Transactional Outbox Pattern
- **Completed:** 2026-06-26
- **Notes:** OutboxEvent entity, WatchlistOutboxListener (BEFORE_COMMIT), OutboxRelay (@Scheduled).

### TASK-005: Kafka Consumer with Idempotency
- **Completed:** 2026-07-05
- **Notes:** ProcessedEvent table, two-tier exception handling. Schema mismatch bug fixed.

### TASK-007: Fix Payload Schema Mismatch
- **Completed:** 2026-07-10
- **Notes:** Wrapped Kafka message with `eventId` and successfully parsed nested payload.

### TASK-008: Fix WatchlistTest
- **Completed:** 2026-07-10
- **Notes:** Updated test to use factory method and corrected assertion string.

### TASK-009: Delete MarketDataWatchlistListener
- **Completed:** 2026-07-10
- **Notes:** Deleted Stage 1 artifact.

### TASK-010: Change WatchlistItemAddedEvent.assetId to UUID
- **Completed:** 2026-07-10
- **Notes:** Extracted UUID from AssetId for cleaner Kafka serialization.

### TASK-011: Merge application.properties into application.yaml
- **Completed:** 2026-07-10
- **Notes:** Merged and deleted properties file.

### TASK-012: Build Watchlist REST API
- **Completed:** 2026-07-12
- **Notes:** Controller + Application Service added.

### TASK-013: Make Watchlist a JPA Entity
- **Completed:** 2026-07-12
- **Notes:** Added JPA annotations and value object converters.

### TASK-014: End-to-End Test
- **Completed:** 2026-07-12
- **Notes:** Full trace verified: HTTP POST → Watchlist aggregate → Outbox → Kafka → Consumer log + ProcessedEvent saved. Also fixed: PostgreSQL port conflict (5432→5433), spring-boot-starter-kafka dependency, SecurityConfig for /api/** access.

### TASK-006: Month 3–4 Event-Driven Architecture
- **Completed:** 2026-07-12
- **Notes:** Milestone complete. All event-driven infrastructure is operational end-to-end.
