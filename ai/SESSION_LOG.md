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

**Next Step:** MVP Sprint Backend: TASK-020 (Asset Registry & Mock Users) and TASK-021 (Watchlist CRUD API).

---

## Session 6 — 2026-08-01

**Summary:** Completed MVP Backend API Sprint (TASK-020 & TASK-021). Created Asset Registry with real stock data, Mock Users endpoint, full Watchlist CRUD endpoints, global CORS configuration, and fixed ArchUnit & Jackson 3.x namespace compatibility.

**Work Completed:**
- Created `assets.json` with 50 real US stocks across tech, financials, healthcare, energy, consumer, industrial, and media sectors
- Built `AssetInfo` record and `AssetRegistry` service to load stock dataset deterministically using `UUID.nameUUIDFromBytes`
- Built `AssetController` with `GET /api/v1/assets`, `GET /api/v1/assets/{id}`, and `GET /api/v1/assets/search?q=`
- Built `MockUserController` in `user/infrastructure` returning mock users (`GET /api/v1/users/mock`)
- Created `WatchlistResponse` DTO to prevent exposing domain aggregate roots over REST
- Extended `WatchlistRepository` with `findByOwnerId(UserId)` derived query
- Extended `WatchlistService` and `WatchlistController` with `GET /api/v1/watchlists?ownerId=`, `GET /api/v1/watchlists/{id}`, `DELETE /api/v1/watchlists/{id}`, and `DELETE /api/v1/watchlists/{id}/assets/{assetId}`
- Configured global CORS in `SecurityConfig` for `http://localhost:3000`
- Updated ArchUnit `ArchitectureEnforcementTest` to scope rule to application packages

**Files Created:**
- `src/main/resources/data/assets.json`
- `marketdata/application/AssetInfo.java`
- `marketdata/application/AssetRegistry.java`
- `marketdata/infrastructure/AssetController.java`
- `user/infrastructure/MockUserController.java`
- `watchlist/infrastructure/WatchlistResponse.java`
- `ai/handbook_july26/session6_handbook_mvp_backend.md`

**Files Modified:**
- `SecurityConfig.java`
- `watchlist/infrastructure/WatchlistRepository.java`
- `watchlist/application/WatchlistService.java`
- `watchlist/infrastructure/WatchlistController.java`
- `src/test/java/org/workshop/marketcanvas/ArchitectureEnforcementTest.java`

**Key Decisions:**
- ADR-010: Static Asset Registry Over Live API Integration for MVP

**Key Lessons:**
- Spring Boot 4.x Jackson 3.x namespace (`tools.jackson`) must be consistently used across all JSON deserialization beans (e.g. `TypeReference`).
- ArchUnit `layeredArchitecture().consideringOnlyDependenciesInAnyPackage(...)` is required when enforcing bounded context isolation so framework/JDK classes aren't flagged as illegal cross-layer dependencies.

**Next Step:** TASK-022: Build Next.js Frontend (MVP Dashboard).

---

## Session 7 — 2026-08-01

**Summary:** Built full Next.js 14+ Frontend Application, multi-stage Docker containerization for the entire stack, unified `docker-compose.yml`, and an in-app interactive REST API Test Bench.

**Work Completed:**
- Created Next.js 14+ project in `frontend/` with TypeScript, Tailwind CSS, Lucide icons, and custom Bloomberg-style fintech aesthetic
- Built typed REST client in `src/lib/api.ts` covering all 10 Spring Boot endpoints
- Built `UserContext.tsx` providing dynamic mock identity switching (Alice, Bob, Carol) and `localStorage` persistence
- Built `Navbar.tsx` with live backend connectivity telemetry, user switcher, and quick API tester drawer
- Built `page.tsx` (Dashboard) with active user portfolio metrics, watchlist card grid, and `CreateWatchlistModal.tsx`
- Built `watchlists/[id]/page.tsx` with dynamic breadcrumbs, capacity enforcement gauge (10 assets max invariant), and `AssetTable.tsx`
- Built SVG mini `Sparkline.tsx` and mock pricing simulator for real-time market feels
- Built `AssetSearchModal.tsx` and `assets/page.tsx` (Market Directory) with live search and sector filtering across the 50 verified US equities
- Built `EndpointTesterDrawer.tsx` (In-App API Inspector & Test Bench) enabling 1-click execution, latency telemetry, and JSON inspection of all 10 endpoints
- Created root multi-stage `Dockerfile` (Spring Boot builder + eclipse-temurin runner) and `frontend/Dockerfile` (Node standalone runner)
- Updated `docker-compose.yml` with dual Kafka listeners (`PLAINTEXT://kafka:9092`, `PLAINTEXT_HOST://localhost:9092`), backend on `:8080`, and frontend on `:3000` with healthchecks
- Verified Next.js production build (`npm run build`) with static and dynamic App Router routes
- Verified backend unit and ArchUnit tests (`.\mvnw.cmd test "-Dtest=WatchlistTest,ArchitectureEnforcementTest"`)

**Files Created:**
- `Dockerfile`
- `.dockerignore`
- `frontend/Dockerfile`
- `frontend/.dockerignore`
- `frontend/package.json`
- `frontend/tsconfig.json`
- `frontend/next.config.mjs`
- `frontend/postcss.config.mjs`
- `frontend/tailwind.config.ts`
- `frontend/src/app/globals.css`
- `frontend/src/app/layout.tsx`
- `frontend/src/app/page.tsx`
- `frontend/src/app/assets/page.tsx`
- `frontend/src/app/watchlists/[id]/page.tsx`
- `frontend/src/types/index.ts`
- `frontend/src/lib/api.ts`
- `frontend/src/lib/mockPrices.ts`
- `frontend/src/context/UserContext.tsx`
- `frontend/src/components/layout/Navbar.tsx`
- `frontend/src/components/user/UserSwitcher.tsx`
- `frontend/src/components/watchlist/Sparkline.tsx`
- `frontend/src/components/watchlist/WatchlistCard.tsx`
- `frontend/src/components/watchlist/AssetTable.tsx`
- `frontend/src/components/watchlist/CreateWatchlistModal.tsx`
- `frontend/src/components/watchlist/AssetSearchModal.tsx`
- `frontend/src/components/api-inspector/EndpointTesterDrawer.tsx`

**Files Modified:**
- `docker-compose.yml`
- `ai/TASKS.md`
- `ai/CURRENT_STATE.md`
- `ai/SESSION_LOG.md`

**Next Step:** Ready for TASK-017 (Real Stock Market Data Ingestion with Finnhub/Yahoo fallback, 2–3 daily snapshot scheduler, and Kafka `platform.marketdata.prices` publishing).

