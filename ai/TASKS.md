# Task Board — MarketCanvas

> Living task board. Update after every session.  
> **Last updated:** 2026-07-25

---

## 🔴 Blocked

*None currently.*

---

## 🟡 In Progress

*None currently.*

---

## 📋 Todo

### TASK-020: Asset Registry + Mock Users (Backend)
- **Priority:** 🔴 HIGH
- **Dependencies:** None
- **Notes:** Create `AssetRegistry` service with ~50 real US stocks from a static JSON file. Expose via `GET /api/v1/assets/search?q=`. Create `MockUserController` with 3-4 demo users at `GET /api/v1/users/mock`.

### TASK-021: Complete Watchlist CRUD API (Backend)
- **Priority:** 🔴 HIGH
- **Dependencies:** None
- **Notes:** Add GET (list, detail), DELETE (watchlist, asset) endpoints. Add `WatchlistResponse` DTO. Add CORS for localhost:3000. Add `findByOwnerId` to repository.

### TASK-022: Next.js Frontend (MVP Dashboard)
- **Priority:** 🔴 HIGH
- **Dependencies:** TASK-020, TASK-021
- **Notes:** Build Next.js app with Dashboard (user selector, watchlist list, create) and Watchlist Detail (asset search, add/remove). Dark theme, real stock tickers.

### TASK-017: Market Data Ingestion Producer
- **Priority:** 🟡 MEDIUM
- **Dependencies:** TASK-022 (MVP Sprint gate)
- **Notes:** As per PDF Month 3-4, Market Data must act as a producer. Implement a scheduled job that fetches mock prices and publishes `PriceUpdated` events to Kafka.

### TASK-018: Event Schema Design (Avro / JSON Schema)
- **Priority:** 🟡 MEDIUM
- **Dependencies:** None
- **Notes:** As per PDF Month 3-4, we need formal schema design. Introduce Confluent Schema Registry to docker-compose and migrate `StringSerializer` to `KafkaAvroSerializer` or `KafkaJsonSchemaSerializer`.

### TASK-019: Kafka Connect to S3 (Archive)
- **Priority:** 🟡 MEDIUM
- **Dependencies:** None
- **Notes:** As per PDF Month 3-4, set up Kafka Connect in docker-compose with an S3 sink connector (use MinIO locally) to archive all domain events for the cold storage tier.

---

## ✅ Completed

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
