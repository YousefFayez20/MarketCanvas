# Architecture — MarketCanvas

## Architecture Evolution Strategy

The platform evolves through three deliberate stages. **Never skip a stage.**

| Stage | Name | When to Advance |
|-------|------|----------------|
| 1 | Modular Monolith | Current → until >10K users or clear bottleneck |
| 2 | Event-Driven System | When modules need async communication |
| 3 | Distributed Microservices | When independent scaling/deployment is needed |

**Current Stage: Transitioning from Stage 1 → Stage 2**

## Overall Architecture

```
┌────────────────────────────────────────────────────────┐
│                    Next.js Frontend                     │
│                    (Not started)                        │
└─────────────────────────┬──────────────────────────────┘
                          │ REST API (planned)
┌─────────────────────────▼──────────────────────────────┐
│              Spring Boot Modular Monolith               │
│                                                         │
│  ┌──────────┐ ┌──────────┐ ┌────────────┐ ┌─────────┐ │
│  │   User   │ │ Watchlist │ │ MarketData │ │ Thesis  │ │
│  │  Module  │ │  Module   │ │   Module   │ │ Module  │ │
│  └────┬─────┘ └────┬─────┘ └─────┬──────┘ └────┬────┘ │
│       │             │             │              │      │
│       └─────────────┴──────┬──────┴──────────────┘      │
│                            │                            │
│                   Shared Kernel                         │
│              (Value Objects, Events)                    │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │           Outbox Pattern (Infrastructure)        │   │
│  │  Spring Event → Outbox Table → Kafka Publisher   │   │
│  └──────────────────────┬──────────────────────────┘   │
└─────────────────────────┼──────────────────────────────┘
                          │
                ┌─────────▼─────────┐
                │   Apache Kafka    │
                │   (KRaft mode)    │
                └─────────┬─────────┘
                          │
                ┌─────────▼─────────┐
                │    Kafka Consumer  │
                │  (Idempotent via   │
                │  ProcessedEvent)   │
                └───────────────────┘

┌─────────────────┐  ┌──────────────┐
│  PostgreSQL 16  │  │ Redis (TBD)  │
│  (Primary DB)   │  │ (Cache)      │
└─────────────────┘  └──────────────┘
```

## Package Organization

```
src/main/java/org/workshop/marketcanvas/
├── MarketCanvasApplication.java          ← Entry point (@EnableScheduling)
│
├── sharedkernel/                         ← Cross-cutting concerns
│   ├── domain/
│   │   ├── UserId.java                   ← Value Object (record)
│   │   ├── AssetId.java                  ← Value Object (record)
│   │   └── OutboxEvent.java              ← JPA Entity (outbox table)
│   ├── events/
│   │   └── WatchlistItemAddedEvent.java  ← Domain Event (record)
│   └── infrastructure/
│       ├── OutboxEventRepository.java    ← JPA Repository
│       ├── OutboxRelay.java              ← @Scheduled Kafka publisher
│       └── WatchlistOutboxListener.java  ← @TransactionalEventListener
│
├── user/                                 ← User Management Context
│   ├── application/
│   ├── domain/
│   └── infrastructure/
│
├── watchlist/                            ← Watchlist Context
│   ├── application/
│   ├── domain/
│   │   └── Watchlist.java                ← Aggregate Root
│   └── infrastructure/
│
└── marketdata/                           ← Market Data Context
    ├── application/
    │   ├── MarketDataWatchlistListener.java  ← DEPRECATED (Stage 1 artifact)
    │   └── messaging/
    │       └── WatchlistEventConsumer.java   ← Kafka consumer (idempotent)
    └── infrastructure/
        └── messaging/
            ├── ProcessedEvent.java           ← Idempotency tracking entity
            └── ProcessedEventRepository.java
```

## Module Boundaries (Enforced)

Modules communicate through **events only**. Direct cross-module class imports are forbidden and enforced by ArchUnit tests.

```
Rule: Watchlist → may only access → SharedKernel
Rule: User     → may only access → SharedKernel
Rule: MarketData → may only access → SharedKernel
```

**Violations cause test failure.**

## Data Flow — Watchlist Item Added

```
1. User calls REST API → addAsset(assetId)
2. WatchlistApplicationService fetches subscription tier
3. Watchlist.addAsset(assetId) enforces business rules
4. Aggregate queues WatchlistItemAddedEvent internally
5. repository.save(watchlist) triggers @DomainEvents
6. WatchlistOutboxListener intercepts event (BEFORE_COMMIT phase)
7. OutboxEvent saved to outbox_events table (SAME transaction)
8. Transaction commits → both Watchlist and OutboxEvent are persisted atomically
9. OutboxRelay polls every 5 seconds, finds unprocessed events
10. Sends to Kafka topic: platform.watchlist.events
11. Marks OutboxEvent as processed
12. WatchlistEventConsumer receives from Kafka
13. Idempotency check: does ProcessedEvent exist for this eventId?
14. If new → execute business logic → save ProcessedEvent marker
15. If duplicate → log and skip
```

## Database Schema

### Tables (Auto-generated by Hibernate `ddl-auto: update`)

| Table | Owner Module | Purpose |
|-------|-------------|---------|
| `outbox_events` | SharedKernel | Transactional Outbox for event publishing |
| `processed_events` | MarketData | Idempotency tracking for Kafka consumers |
| *(Watchlist tables)* | Watchlist | Stores watchlists and their contained assets |

### Database: PostgreSQL 16
- Host: `localhost:5432`
- Database: `investment_platform`
- User: `postgres` / `postgres`

## Kafka Topics

| Topic | Partition Key | Retention | Producers | Consumers |
|-------|--------------|-----------|-----------|-----------|
| `platform.watchlist.events` | `aggregateId` (watchlistId) | Default | OutboxRelay | WatchlistEventConsumer |
| `platform.watchlist.events.dlq` | `partition` | 7 days | DeadLetterPublishingRecoverer | Ops monitoring |

## Authentication & Authorization

**Not yet implemented.** Planned: OAuth2 + OIDC (Google/Apple/Microsoft), JWT with short expiry, RBAC roles (FREE_USER, PRO_USER, TEAM_ADMIN, PLATFORM_ADMIN).

## Frontend

**Not yet started.** Planned: Next.js.

## Deployment

**Local development only.** Docker Compose with:
- Kafka (Bitnami, KRaft mode) on port 9092
- PostgreSQL 16 on port 5432
