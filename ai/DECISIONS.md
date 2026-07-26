# Architecture Decision Records (ADR)

> Every significant technical decision is recorded here. Future AI must never reverse these decisions without explicit user approval and documented justification.

---

## ADR-001: Modular Monolith Over Microservices

**Date:** 2026-06-22  
**Status:** Active  

**Decision:** Start with a Modular Monolith architecture.

**Reason:**
- Domain boundaries are still being discovered. Wrong boundaries are cheap to fix in a monolith, expensive in microservices.
- Team size is 1. Microservices are optimized for multiple autonomous teams.
- Single JVM process is easier to debug, deploy, and monitor.
- No measurable scaling bottleneck exists yet.

**Alternatives Considered:**
- Microservices from day one — rejected (premature complexity, 60-70% failure rate for startups).
- Standard layered monolith (controllers/services/repos) — rejected (becomes spaghetti in 6 months).

**Trade-offs:**
- ✅ Deployment simplicity, refactoring speed, debugging ease
- ❌ Cannot independently scale modules, all modules share JVM resources

**Future Impact:** Modules are structured identically to microservices. Extraction requires moving a package and replacing Spring events with network calls. Expected at >10,000 active users.

---

## ADR-002: PostgreSQL Over MySQL

**Date:** 2026-06-26  
**Status:** Active  

**Decision:** Use PostgreSQL 16 as the primary database.

**Reason:**
- `pgvector` extension is required for RAG pipeline (vector similarity search) in Month 5-6.
- `TimescaleDB` extension is required for time-series price data.
- Both are PostgreSQL-only. Starting with MySQL would require a full migration later.

**Alternatives Considered:**
- MySQL — rejected (no pgvector, no TimescaleDB)
- MongoDB — rejected (ACID transactions needed for financial data)

**Trade-offs:**
- ✅ Future-proof for AI and time-series features
- ❌ Slightly more complex tuning for high write throughput compared to MySQL

**Future Impact:** Enables `pgvector` for embeddings and `TimescaleDB` for price history without database migration.

---

## ADR-003: Transactional Outbox Pattern Over Direct Kafka Publish

**Date:** 2026-06-26  
**Status:** Active  

**Decision:** Use the Transactional Outbox Pattern (database + polling relay) for event publishing.

**Reason:**
- Solves the Dual-Write Problem. You cannot reliably write to PostgreSQL AND Kafka in one transaction. If the DB commits but Kafka fails, the system is permanently inconsistent.
- The Outbox table is written in the same DB transaction as the business entity, guaranteeing atomicity.

**Alternatives Considered:**
- Direct `KafkaTemplate.send()` after DB commit — rejected (dual-write problem)
- Debezium (CDC) — rejected for now (operational complexity, requires WAL configuration)
- Spring Modulith's built-in event externalization — considered for future

**Trade-offs:**
- ✅ Guaranteed consistency between DB state and published events
- ❌ Up to 5 seconds latency (polling interval), extra DB table, extra scheduled task

**Future Impact:** Can be replaced with Debezium (CDC) in Stage 3 to eliminate polling and reduce latency.

---

## ADR-004: Kafka KRaft Mode Over Zookeeper

**Date:** 2026-06-26  
**Status:** Active  

**Decision:** Use Kafka in KRaft mode (no Zookeeper).

**Reason:**
- KRaft is the modern standard. Zookeeper is deprecated in Kafka.
- Simpler Docker Compose setup (one container instead of two).

**Alternatives Considered:**
- Zookeeper-based Kafka — rejected (deprecated)
- RabbitMQ — rejected (not a replayable log, weaker event sourcing support)
- AWS SQS — rejected (vendor lock-in, no replay)

**Trade-offs:**
- ✅ Simpler ops, future-proof
- ❌ Slightly fewer community examples for KRaft vs Zookeeper setups

---

## ADR-005: Spring Application Events → Kafka (Gradual Migration)

**Date:** 2026-06-22 → 2026-06-26  
**Status:** Active  

**Decision:** Start with Spring `ApplicationEventPublisher` for inter-module communication, then transition to Kafka via the Outbox Pattern.

**Reason:**
- Stage 1 used synchronous Spring events to discover event schemas and domain boundaries.
- Stage 2 replaces internal events with Kafka for operational decoupling and resilience.
- Gradual migration reduces risk.

**Alternatives Considered:**
- Jump straight to Kafka from day one — rejected (premature, adds complexity before events are well-defined)

---

## ADR-006: Idempotent Consumer via Database Table

**Date:** 2026-07-05  
**Status:** Active  

**Decision:** Track processed Kafka messages in a `processed_events` database table for idempotency.

**Reason:**
- Kafka guarantees at-least-once delivery. Consumers WILL receive duplicates.
- The `ProcessedEvent` check and business logic run in the same `@Transactional` boundary. If the transaction rolls back, the marker rolls back too — guaranteeing exactly-once processing semantics.

**Alternatives Considered:**
- Kafka Exactly-Once Semantics (EOS) — rejected (only works for Kafka-to-Kafka, not Kafka-to-PostgreSQL)
- Relying on offset commits alone — rejected (offset can commit before processing completes)

**Trade-offs:**
- ✅ Simple, reliable, works with any downstream data store
- ❌ Extra DB query per message, `processed_events` table grows over time (needs cleanup job)

---

## ADR-007: Package-by-Domain Over Package-by-Layer

**Date:** 2026-06-22  
**Status:** Active  

**Decision:** Structure packages by business domain (e.g., `watchlist/`, `marketdata/`) not by technical layer (e.g., `controllers/`, `services/`).

**Reason:**
- Each bounded context is a self-contained module with its own domain, application, and infrastructure layers.
- This structure maps directly to future microservice extraction.
- ArchUnit tests enforce boundaries at the package level.

**Alternatives Considered:**
- Package-by-layer — rejected (cross-cutting dependencies, spaghetti at scale)

---

## ADR-008: Two-Tier Exception Handling in Kafka Consumers

**Date:** 2026-07-05  
**Status:** Active  

**Decision:** Kafka consumers use two catch blocks:
1. `IllegalArgumentException` (Poison Pill) → log and skip
2. `Exception` (transient error) → re-throw to trigger Kafka retry

**Reason:**
- Malformed messages that can never be parsed must be skipped, or they block the entire partition forever (infinite retry loop).
- Transient errors (DB timeout, network blip) should be retried automatically by Kafka's consumer retry mechanism.

**Future Impact:** Will be enhanced with a Dead Letter Queue (DLQ) so skipped poison pills are not silently lost.

---

## ADR-009: Dead Letter Queue (DLQ) for Poison Pills

**Date:** 2026-07-25  
**Status:** Active  

**Decision:** Route failed Kafka messages to a Dead Letter Queue (DLQ) topic instead of logging and dropping them, or retrying infinitely.

**Reason:**
- Silent loss of data (logging and dropping) is unacceptable in a financial application.
- Infinite retries block the partition and halt processing of healthy messages.
- A DLQ preserves the original message and error headers for manual inspection, replay, or automated recovery.

**Alternatives Considered:**
- Manual `try-catch` with `.send()` to DLQ — rejected (boilerplate, easy to get wrong)
- Discarding messages — rejected (data loss)

**Trade-offs:**
- ✅ Zero data loss, unblocks partitions, preserves context
- ❌ Requires a separate consumer or manual process to inspect and replay the DLQ

**Future Impact:** We can build tooling later to replay messages from the DLQ once the underlying bugs are fixed.
