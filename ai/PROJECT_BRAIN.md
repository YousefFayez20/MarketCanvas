# PROJECT_BRAIN.md

> **Last Updated:** 2026-07-12
> **Purpose:** This document is the project's long-term engineering memory and intellectual history. It captures the engineering understanding behind the code, minimizing knowledge loss between AI sessions.

---

## 1. Project Evolution

The MarketCanvas project started as an idea for an AI Financial Intelligence Platform, designed to help investors separate signal from noise.

**Milestone 1: The Foundation (Modular Monolith)**
We began by establishing a solid Domain-Driven Design (DDD) foundation. We explicitly rejected a microservices-first approach to avoid premature complexity, opting instead for a strict Modular Monolith (ADR-001). We used `Spring Modulith` and `ArchUnit` to enforce package-by-domain boundaries (`user`, `watchlist`, `marketdata`, `sharedkernel`). We focused on rich aggregate roots (e.g., `Watchlist`) that protect their own invariants and use Value Objects (`UserId`, `AssetId`) for type safety.

**Milestone 2: Event-Driven Infrastructure (Stage 1 to Stage 2)**
To decouple bounded contexts without extracting microservices, we introduced Domain Events. Initially using Spring's `@DomainEvents` and synchronous listeners, we quickly pivoted to an Event-Driven Architecture (EDA) using Apache Kafka to ensure durability and scalability.
To solve the dual-write problem (updating the database and sending a Kafka message reliably), we implemented the **Transactional Outbox Pattern** (ADR-003).

**Milestone 3: Consumer Idempotency & Resilience**
As we integrated Kafka, we recognized the need for exactly-once processing semantics. We implemented an Idempotent Consumer using a `ProcessedEvent` database table (ADR-006) and established a two-tier exception handling strategy (ADR-008) to differentiate between transient errors (which should be retried) and poison pills (which should be skipped or sent to a DLQ).

**Milestone 4: JPA Entity Mapping, REST API & End-to-End Verification (2026-07-12)**
We transformed the `Watchlist` aggregate into a full JPA entity with `@ElementCollection` for the asset set, and created `AttributeConverter`s (`UserIdConverter`, `AssetIdConverter`) to bridge our Value Object records with PostgreSQL UUIDs. We built the Application Service (`WatchlistService`) and REST Controller (`WatchlistController`) layers. We also added a `SecurityConfig` to permit `/api/**` access during development. After resolving a PostgreSQL port conflict (native Windows PG on 5432, Docker remapped to 5433) and fixing the Kafka dependency (`spring-kafka` → `spring-boot-starter-kafka` for auto-configuration in Spring Boot 4.x), we successfully ran the **first end-to-end test**: `curl → REST API → Aggregate → Outbox → Kafka → Consumer → ProcessedEvent`. This milestone marks the completion of the foundational Event-Driven Architecture.

---

## 2. Complete Feature Inventory

### 2.1 Watchlist Management
* **Purpose:** Allows users to track financial assets. Protects business invariants (e.g., free tier users are limited to 10 assets).
* **Location:** `watchlist` bounded context.
* **Related Entities:** `Watchlist` (Aggregate Root), `UserId`, `AssetId`.
* **Important Classes:** `Watchlist.java` (factory method, invariant checks), `WatchlistItemAddedEvent`.
* **Dependencies:** `sharedkernel` (for value objects and event contracts).
* **Important Classes:** `Watchlist.java` (JPA Entity, factory method, invariant checks), `WatchlistItemAddedEvent`, `WatchlistService` (Application Service), `WatchlistController` (REST API), `WatchlistRepository` (Spring Data JPA).
* **REST Endpoints:** `POST /api/v1/watchlists` (create), `POST /api/v1/watchlists/{id}/assets` (add asset).
* **Future Improvements:** GET endpoints, pagination for large watchlists, Pro tier with unlimited assets.

### 2.2 Transactional Outbox
* **Purpose:** Guarantees that domain events are reliably published to Kafka without dual-write inconsistencies.
* **Location:** `sharedkernel` bounded context.
* **Important Classes:** `OutboxEvent` (Entity), `WatchlistOutboxListener` (captures domain events `BEFORE_COMMIT`), `OutboxRelay` (polls and publishes to Kafka).
* **Dependencies:** Spring Data JPA, Spring Kafka, PostgreSQL.
* **Future Improvements:** Replace polling with Change Data Capture (Debezium) for lower latency.

### 2.3 Idempotent Event Consumption
* **Purpose:** Ensures that Kafka messages are processed exactly once, even in the event of consumer retries or duplicate deliveries.
* **Location:** `marketdata` bounded context.
* **Important Classes:** `WatchlistEventConsumer`, `ProcessedEvent` (Entity).
* **Dependencies:** Spring Kafka, Spring Data JPA, PostgreSQL.
* **Future Improvements:** Automatic cleanup of old `ProcessedEvent` records to prevent unbounded table growth.

---

## 3. Code Map

The repository is structured as a Modular Monolith, organized strictly by bounded context rather than technical layer.

* **`/sharedkernel`**: Contains concepts used across multiple domains.
  * `/domain`: Value objects like `UserId` and `AssetId`. Domain event interfaces.
  * `/infrastructure`: The Outbox Pattern implementation (`OutboxEvent`, `OutboxRelay`).
* **`/watchlist`**: The core domain for managing user asset lists.
  * `/domain`: The `Watchlist` aggregate root. It encapsulates all business rules regarding watchlists (e.g., asset limits).
* **`/marketdata`**: Responsible for consuming events from the watchlist and fetching external financial data.
  * `/application/messaging`: Kafka consumers like `WatchlistEventConsumer`.
  * `/infrastructure/messaging`: Idempotency tracking (`ProcessedEvent`).
* **`/user`**: Identity and access management (planned).

---

## 4. Engineering Concepts Used

* **Domain-Driven Design (DDD):** Applied throughout the codebase. Use of Aggregate Roots (`Watchlist`), Value Objects (`UserId`, `AssetId`), and Domain Events (`WatchlistItemAddedEvent`) to model complex business logic clearly.
* **Modular Monolith:** Chosen over microservices to reduce operational overhead while maintaining strict logical boundaries via `ArchUnit`.
* **Transactional Outbox Pattern:** Used to solve the dual-write problem. Domain state changes and outbox events are committed in a single database transaction. A background relay publishes to Kafka.
* **Event-Driven Architecture (EDA):** Bounded contexts communicate asynchronously via Kafka, minimizing temporal coupling.
* **Idempotent Consumers:** Kafka consumers track processed event IDs in a PostgreSQL table to safely handle retries and duplicate messages.
* **Two-Tier Exception Handling:** In Kafka consumers, we distinguish between business/validation errors (Poison Pills - skipped) and infrastructure errors (Transient - retried).

---

## 5. Domain Knowledge

**Core Business Problem:** Helping Active Investors filter "signal" from "noise" in financial markets.
**Key Entities:**
* **Watchlist:** A user's curated list of monitored assets. Crucially, the Free Tier limits this to 10 assets to drive Pro conversion.
* **Asset:** A financial instrument (Equity, ETF, Commodity, etc.). Represented internally by `AssetId`.
**Workflows:**
* Adding an asset: The `Watchlist` aggregate verifies the 10-asset limit invariant before mutating state and emitting a `WatchlistItemAddedEvent`.

---

## 6. Architecture Knowledge

* **Package-by-Domain:** We organize code by business capability (`/watchlist`, `/marketdata`) rather than technical layer (`/controllers`, `/services`). This increases cohesion and makes extracting microservices later much easier.
* **Synchronous vs. Asynchronous:** Inside a bounded context, communication is synchronous (method calls). Between bounded contexts, communication is asynchronous (Kafka events).
* **Why PostgreSQL?** We chose PostgreSQL 16 because of its robust transactional support (needed for the Outbox Pattern) and its extensive ecosystem (specifically `pgvector` for upcoming AI RAG features and `TimescaleDB` for time-series price data).

---

## 7. Important Algorithms

*(Currently, most logic is standard CRUD and invariant checking. As the AI Research Workspace and Thesis Scoring systems are built, algorithms for text chunking, embedding similarity search, and score calculation will be documented here.)*

* **Outbox Polling:** The `OutboxRelay` uses a simple `@Scheduled(fixedDelay=5000)` to poll the database. It uses `findTop100ByProcessedFalseOrderByCreatedAt` to process events in batches, balancing throughput and memory usage.

---

## 8. Design Patterns

* **Factory Method:** Used in `Watchlist.create(...)`. We keep constructors private to ensure aggregates are always created in a valid state with all invariants checked.
* **Transactional Outbox:** (Described in section 4).
* **Value Object:** `UserId` and `AssetId` are implemented as Java `record`s. They provide immutability, structural equality, and self-validation.

---

## 9. Infrastructure Knowledge

* **Local Development:** We use `docker-compose.yml` to spin up PostgreSQL 16 and Kafka (in KRaft mode, eliminating the Zookeeper dependency).
* **Kafka KRaft:** Selected over Zookeeper for a simpler, more modern architecture.
* **PostgreSQL Port:** Docker maps to host port `5433` (not `5432`) because a native Windows PostgreSQL installation occupies port `5432`. Both `docker-compose.yml` and `application.yml` reflect this.
* **Kafka Dependency:** Spring Boot 4.x requires `spring-boot-starter-kafka` (not raw `spring-kafka`) for the `KafkaTemplate` bean to be auto-configured.
* **PowerShell cURL:** On Windows, `curl` aliases to `Invoke-WebRequest`. Use `curl.exe` for Linux-style syntax. Escape JSON with `\"`.

---

## 10. Database Knowledge

* **Schema Philosophy:** Each bounded context theoretically owns its schema, though they currently reside in the same PostgreSQL database.
* **`outbox_events` table:** Stores pending Kafka messages. Needs an index on `processed` and `created_at` for efficient polling.
* **`processed_events` table:** Stores IDs of successfully consumed Kafka messages to ensure idempotency. Needs a unique constraint on `event_id`.

---

## 11. API Knowledge

* **REST Convention:** All API endpoints are under `/api/v1/`. Controllers are thin — they convert HTTP primitives to Value Objects and delegate to Application Services.
* **DTOs:** Java `record`s used for request bodies (`CreateWatchlistRequest`, `AddAssetRequest`). Response bodies return raw UUIDs for simplicity.
* **Security:** `SecurityConfig` permits all `/api/**` requests. CSRF is disabled for REST APIs. This is a development-only configuration — production will require proper JWT/OAuth2 authentication.

---

## 12. Frontend Knowledge

*(Frontend is planned for Phase 2 using Next.js. Documentation will be added as UI development begins.)*

---

## 13. External Integrations

* **Apache Kafka:** Used as the central nervous system for inter-module communication.

---

## 14. Problems Solved

* **The Dual-Write Problem:**
  * *Root Cause:* Updating JPA entities and sending Kafka messages are separate distributed transactions. If the DB commits but Kafka fails, the system is inconsistent.
  * *Solution:* Transactional Outbox Pattern. We save the event to the DB in the same transaction as the aggregate update. A separate relay process guarantees at-least-once delivery to Kafka.
* **Kafka Poison Pills:**
  * *Root Cause:* A malformed message causes deserialization or validation to fail. If the consumer throws an exception, Kafka will retry indefinitely, blocking the partition.
  * *Solution:* Dead Letter Queue (DLQ) with Spring Kafka's `DefaultErrorHandler`. We configure 3 retries for transient errors (e.g., DB timeouts), after which the message is routed to a `.dlq` topic using `DeadLetterPublishingRecoverer`. Fundamentally broken messages (Poison Pills like `JacksonException` or `IllegalArgumentException`) are marked as non-retryable and routed to the DLQ immediately.

---

## 15. Lessons Learned

* **Event Schema Evolution:** We learned that using complex domain objects (like `AssetId` records) inside domain events causes serialization issues when crossing boundaries. Events must use primitive types (like `UUID` and `String`) to maintain clean JSON contracts. (See Bug: TASK-010).
* **Testing Aggregates:** Unit tests should interact with aggregates exclusively through their public API (factory methods and behavior methods). Attempting to use private constructors in tests leads to brittle tests that break during refactoring. (See Bug: TASK-008).
* **Port Conflicts in Docker:** When mapping Docker container ports to the host, always check if a native service is already occupying that port. On Windows, a native PostgreSQL installation silently intercepts connections to `localhost:5432`, causing authentication failures even with a freshly created Docker volume. The fix is to remap to an alternate port (e.g., `5433`).
* **Spring Boot Starters vs Raw Libraries:** In Spring Boot, always prefer `spring-boot-starter-*` over raw library dependencies. The starter includes the auto-configuration class that reads your `application.yml` and creates the necessary beans. Without it, you get the Java classes but no beans — leading to confusing `NoSuchBeanDefinitionException` errors.
* **Know Your Exceptions:** When configuring non-retryable exceptions for a DLQ, knowing the exact exception class is critical. We initially assumed malformed JSON would throw `IllegalArgumentException`, but `tools.jackson` throws `JacksonException`. This caused poison pills to be retried instead of being sent to the DLQ immediately.

---

## 16. Common Pitfalls

* **Leaking Domain Objects:** Never expose Domain Entities or Value Objects in REST Controllers or Kafka event payloads. Always map to primitive DTOs to prevent tight coupling.
* **Forgetting `@EnableScheduling`:** The Outbox Relay won't run unless this annotation is present on a configuration class.
* **Jackson Checked Exceptions:** When using Spring Boot 4.x / Jackson 3.x, `ObjectMapper.writeValueAsString()` throws a checked exception. It must be caught and wrapped in a `RuntimeException` inside the `@TransactionalEventListener`.

---

## 17. Glossary of Internal Knowledge

See `/ai/GLOSSARY.md` for a complete list of terms.

---

## 18. Cross References

For deeper details, consult:
* **Overall Architecture:** `/ai/ARCHITECTURE.md`
* **Why we chose this path:** `/ai/DECISIONS.md`
* **How to write code here:** `/ai/PATTERNS.md`
* **What to do next:** `/ai/TASKS.md`
