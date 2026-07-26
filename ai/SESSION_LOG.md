# Session Log — MarketCanvas

> Append-only chronological log. Every session adds an entry at the bottom.  
> This is the AI's memory across conversations.

---

## Session 1 — 2026-06-22

**Summary:** Project initialization. Established the Modular Monolith foundation with DDD principles.

**Work Completed:**
- Created Spring Boot project (Java 21, Spring Boot 4.1)
- Established package-by-domain structure: `user`, `watchlist`, `marketdata`, `sharedkernel`
- Implemented `UserId` and `AssetId` as Java record value objects
- Built `Watchlist` aggregate root with factory method, business invariants (max 10 assets, name ≤50 chars), and `@DomainEvents`
- Created `ArchitectureEnforcementTest` using ArchUnit
- Created `WatchlistItemAddedEvent` domain event record
- Created initial `MarketDataWatchlistListener` (Spring event listener for Stage 1)

**Files Created:**
- `MarketCanvasApplication.java`
- `sharedkernel/domain/UserId.java`
- `sharedkernel/domain/AssetId.java`
- `sharedkernel/events/WatchlistItemAddedEvent.java`
- `watchlist/domain/Watchlist.java`
- `marketdata/application/MarketDataWatchlistListener.java`
- `ArchitectureEnforcementTest.java`

**Key Decisions:**
- ADR-001: Modular Monolith over Microservices
- ADR-007: Package-by-domain over Package-by-layer

**Next Step:** Implement the Transactional Outbox Pattern and introduce Kafka.

---

## Session 2 — 2026-06-26

**Summary:** Introduced event-driven infrastructure. Built the Outbox Pattern and Kafka setup.

**Work Completed:**
- Added `spring-kafka` dependency
- Swapped MySQL driver for PostgreSQL driver (ADR-002)
- Created `OutboxEvent` JPA entity with Lombok annotations
- Created `OutboxEventRepository` with batch query
- Created `WatchlistOutboxListener` (`@TransactionalEventListener`, `BEFORE_COMMIT` phase)
- Created `OutboxRelay` (`@Scheduled` publisher to Kafka)
- Created `docker-compose.yml` with Kafka (KRaft mode) and PostgreSQL 16
- Created `application.yaml` with Kafka + JPA configuration
- Fixed `@EnableScheduling` on `MarketCanvasApplication`
- Fixed Jackson exception handling in `WatchlistOutboxListener`

**Files Created:**
- `sharedkernel/domain/OutboxEvent.java`
- `sharedkernel/infrastructure/OutboxEventRepository.java`
- `sharedkernel/infrastructure/OutboxRelay.java`
- `sharedkernel/infrastructure/WatchlistOutboxListener.java`
- `docker-compose.yml`
- `application.yaml`

**Files Modified:**
- `MarketCanvasApplication.java` (added `@EnableScheduling`)
- `pom.xml` (added spring-kafka, swapped MySQL → PostgreSQL)

**Key Decisions:**
- ADR-002: PostgreSQL over MySQL
- ADR-003: Transactional Outbox Pattern
- ADR-004: Kafka KRaft mode
- ADR-005: Gradual Spring Events → Kafka migration

**Bugs Identified:**
- Missing `@EnableScheduling` (fixed)
- Jackson checked exception not handled (fixed)

**Next Step:** Build idempotent Kafka consumer.

---

## Session 3 — 2026-07-05

**Summary:** Full code review. Built the Kafka consumer with idempotency. Identified critical bugs.

**Work Completed:**
- Created `WatchlistEventConsumer` with `@KafkaListener` and `@Transactional`
- Implemented two-tier exception handling (poison pill vs transient errors)
- Created `ProcessedEvent` JPA entity for idempotency tracking
- Created `ProcessedEventRepository`
- Created `WatchlistTest` unit test (has bugs — see below)
- Full architectural review of all 13 source files

**Files Created:**
- `marketdata/application/messaging/WatchlistEventConsumer.java`
- `marketdata/infrastructure/messaging/ProcessedEvent.java`
- `marketdata/infrastructure/messaging/ProcessedEventRepository.java`
- `WatchlistTest.java` (test)

**Key Decisions:**
- ADR-006: Idempotent Consumer via Database Table
- ADR-008: Two-tier exception handling in consumers

**Bugs Identified (Not Yet Fixed):**
1. 🔴 Payload schema mismatch — consumer expects `id` field that doesn't exist in serialized event
2. 🔴 `WatchlistTest` uses private constructor instead of factory method
3. 🔴 `WatchlistTest` assertion message doesn't match actual exception text
4. 🟡 `MarketDataWatchlistListener` still active (Stage 1 artifact, causes duplicate handling)
5. 🟡 `AssetId` in event serializes as nested object instead of flat UUID

**AI Context System Created:**
- Built complete `/ai` directory with 16 markdown files as persistent project memory

**Next Step:** Fix the 5 identified bugs, then build the Watchlist REST API for end-to-end testing.

---

## Session 4 — 2026-07-12

**Summary:** Bug fix sweep, JPA entity mapping, REST API, infrastructure stabilization, and **successful end-to-end test**.

**Work Completed:**
- Fixed all 5 bugs from Session 3 (TASK-007 through TASK-011)
- Wrapped Kafka message payload in an envelope with `eventId` and `payload` fields
- Changed `WatchlistItemAddedEvent.assetId` type from `AssetId` to `UUID` for clean serialization
- Fixed `WatchlistTest` to use factory method and corrected assertion string
- Deleted the legacy `MarketDataWatchlistListener` (Stage 1 artifact)
- Merged `application.properties` into `application.yml`
- Made `Watchlist` a full JPA `@Entity` with `@ElementCollection` for assets
- Created `UserIdConverter` and `AssetIdConverter` (`@Converter(autoApply = true)`)
- Created `WatchlistRepository` (Spring Data JPA)
- Created `WatchlistService` (Application Service with `@Transactional`)
- Created `WatchlistController` (REST API: `POST /api/v1/watchlists`, `POST /api/v1/watchlists/{id}/assets`)
- Created `SecurityConfig` to permit `/api/**` access (development only)
- Fixed PostgreSQL port conflict: native Windows PG on 5432, Docker remapped to 5433
- Fixed Kafka dependency: `spring-kafka` → `spring-boot-starter-kafka` for auto-configuration
- **Ran successful end-to-end test:** HTTP POST → Aggregate → Outbox → Kafka → Consumer → ProcessedEvent ✅

**Files Created:**
- `watchlist/infrastructure/WatchlistRepository.java`
- `watchlist/application/WatchlistService.java`
- `watchlist/application/WatchlistController.java`
- `sharedkernel/infrastructure/UserIdConverter.java`
- `sharedkernel/infrastructure/AssetIdConverter.java`
- `SecurityConfig.java`

**Files Modified:**
- `watchlist/domain/Watchlist.java` (added JPA annotations)
- `sharedkernel/events/WatchlistItemAddedEvent.java` (AssetId → UUID)
- `sharedkernel/infrastructure/WatchlistOutboxListener.java` (envelope wrapping)
- `marketdata/application/messaging/WatchlistEventConsumer.java` (envelope parsing)
- `docker-compose.yml` (port 5432 → 5433)
- `application.yml` (port 5432 → 5433)
- `pom.xml` (spring-kafka → spring-boot-starter-kafka)

**Files Deleted:**
- `marketdata/application/MarketDataWatchlistListener.java` (Stage 1 artifact)
- `application.properties` (merged into application.yml)

**Key Lessons:**
- Native Windows services can silently intercept Docker port mappings
- Spring Boot starters include auto-configuration; raw libraries do not
- PowerShell aliases `curl` to `Invoke-WebRequest`; use `curl.exe` for real cURL

**Next Step:** TASK-015 (Dead Letter Queue) and TASK-016 (Kafka Topic Constants).

---

## Session 5 — 2026-07-25

**Summary:** Completed event-driven architecture with Dead Letter Queue (DLQ) and centralized topic constants.

**Work Completed:**
- Extracted Kafka topics into `KafkaTopics` constants class (TASK-016)
- Configured Spring Kafka `DefaultErrorHandler` with 3 retries and 1s backoff
- Added `DeadLetterPublishingRecoverer` to route exhausted retries to `platform.watchlist.events.dlq` (TASK-015)
- Added `IllegalArgumentException`, `IllegalStateException`, and `JacksonException` as non-retryable exceptions (direct to DLQ)
- Verified DLQ behavior using Kafka console consumer and producer

**Files Created:**
- `sharedkernel/infrastructure/KafkaTopics.java`
- `sharedkernel/infrastructure/KafkaConsumerConfig.java`
- `ai/handbook_july23/session5_handbook.md` (Engineering Handbook for DLQ)

**Files Modified:**
- `marketdata/application/messaging/WatchlistEventConsumer.java`
- `sharedkernel/infrastructure/OutboxRelay.java`
- `ai/PROMPTS.md` (Added reusable Engineering Handbook prompt)

**Key Decisions:**
- ADR-009: Dead Letter Queue (DLQ) for Poison Pills

**Key Lessons:**
- Knowing the exact exception type thrown by your libraries is critical for error classification. We initially used `IllegalArgumentException` for malformed JSON, but Jackson throws `JacksonException`. This caused poison pills to be retried instead of being sent directly to the DLQ.

**Next Step:** Phase 1 Cleanup: Event Schema Versioning Strategy (TASK-017).
