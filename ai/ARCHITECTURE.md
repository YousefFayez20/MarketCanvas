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
| `watchlists` / `watchlist_assets` | Watchlist | Stores watchlists and their contained asset IDs |
| `asset_quotes` | MarketData | Real-time / latest snapshot quotes cache (price, change, high, low, volume) |
| `asset_price_history` | MarketData | Historical snapshot records (EOD / intraday checkpoints) |

### Database: PostgreSQL 16
- Host: `localhost:5433` (Docker mapped port; 5432 is reserved for native Windows PG)
- Database: `investment_platform`
- User: `postgres` / `postgres`

## Kafka Topics

| Topic | Partition Key | Retention | Producers | Consumers |
|-------|--------------|-----------|-----------|-----------|
| `platform.watchlist.events` | `aggregateId` (watchlistId) | Default | OutboxRelay | WatchlistEventConsumer |
| `platform.watchlist.events.dlq` | `partition` | 7 days | DeadLetterPublishingRecoverer | Ops monitoring |
| `platform.marketdata.prices` | `ticker` (e.g., AAPL) | 30 days | MarketDataSnapshotScheduler / ResilientMarketDataService | WatchlistValuationConsumer, S3 Archive, Read Caches |
| `platform.marketdata.snapshots` | `snapshotId` | 30 days | MarketDataSnapshotScheduler | Ops, Audit & AI Ingestion Pipelines |

## Market Data Ingestion Pipeline (2-3 Snapshots/Day)

```
1. MarketDataSnapshotScheduler triggers via Cron (09:35, 13:00, 16:05 EST) or manual POST /api/v1/marketdata/refresh
2. ResilientMarketDataProvider queries Finnhub.io (or Yahoo Finance fallback if no token)
3. Quotes are normalized into StockQuote domain records
4. Persisted into asset_quotes and asset_price_history tables
5. StockPriceUpdatedEvent published to Kafka topic: platform.marketdata.prices
6. In-memory read cache updated for sub-millisecond REST queries
7. Downstream consumers update portfolio valuations and event archives
```

## REST API Endpoints

| Context | Endpoint | Method | Purpose |
|---------|----------|--------|---------|
| `watchlist` | `/api/v1/watchlists` | `POST` | Create a new watchlist (returns 201 + UUID) |
| `watchlist` | `/api/v1/watchlists?ownerId={ownerId}` | `GET` | List all watchlists owned by a user |
| `watchlist` | `/api/v1/watchlists/{id}` | `GET` | Get watchlist details and asset IDs |
| `watchlist` | `/api/v1/watchlists/{id}` | `DELETE` | Delete a watchlist (returns 204) |
| `watchlist` | `/api/v1/watchlists/{id}/assets` | `POST` | Add an asset to a watchlist |
| `watchlist` | `/api/v1/watchlists/{id}/assets/{assetId}` | `DELETE` | Remove an asset from a watchlist (returns 204) |
| `marketdata` | `/api/v1/assets/search?q={query}` | `GET` | Search 50 real US stocks by ticker or name |
| `marketdata` | `/api/v1/assets/{id}` | `GET` | Get asset metadata by ID |
| `marketdata` | `/api/v1/assets` | `GET` | Get all available assets in registry |
| `marketdata` | `/api/v1/marketdata/quotes/{ticker}` | `GET` | Get authentic live/latest stock quote |
| `marketdata` | `/api/v1/marketdata/quotes?tickers={list}` | `GET` | Batch fetch authentic stock quotes |
| `marketdata` | `/api/v1/marketdata/refresh` | `POST` | Trigger immediate snapshot ingestion |
| `marketdata` | `/api/v1/marketdata/status` | `GET` | Check data provider status and last snapshot time |
| `user` | `/api/v1/users/mock` | `GET` | Get demo users (Alice, Bob, Carol) |

## Security & CORS

- `/api/**` endpoints permitted without auth (development configuration).
- Global CORS configured in `SecurityConfig` allowing `http://localhost:3000` with `GET`, `POST`, `DELETE`, `OPTIONS`.

## Frontend

**In Progress.** Next.js App Router project under `frontend/` connecting to REST APIs.

## Deployment

**Local development only.** Docker Compose with:
- Kafka (Bitnami, KRaft mode) on port 9092
- PostgreSQL 16 on port 5432
