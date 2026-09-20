# MarketCanvas 📊

> **AI-Powered Financial Intelligence & Real-Time Investment Analytics Platform**

MarketCanvas is an enterprise-grade investment analytics platform engineered to help active investors monitor markets, validate theses, and surface actionable insights. Built with **Spring Boot**, **Spring Modulith**, **Apache Kafka**, and **Spring AI**, MarketCanvas demonstrates modern distributed systems architecture, event-driven design, and resilient real-time data streaming.

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-3.9-red.svg?style=flat&logo=apachekafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7-dc382d.svg?style=flat&logo=redis)](https://redis.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-Google%20Gemini-4285F4.svg?style=flat&logo=google)](https://spring.io/projects/spring-ai)
[![Next.js](https://img.shields.io/badge/Next.js-React-black.svg?style=flat&logo=next.js)](https://nextjs.org/)

---

## 📑 Table of Contents

- [System Architecture](#-system-architecture)
- [Key Features & Engineering Highlights](#-key-features--engineering-highlights)
- [Technology Stack](#-technology-stack)
- [Bounded Contexts & Module Design](#-bounded-contexts--module-design)
- [Event-Driven Data Flow & Outbox Pattern](#-event-driven-data-flow--outbox-pattern)
- [REST & Streaming APIs](#-rest--streaming-apis)
- [Getting Started](#-getting-started)
- [Architecture Enforcement & Testing](#-architecture-enforcement--testing)
- [Configuration Reference](#-configuration-reference)

---

## 🏛 System Architecture

MarketCanvas utilizes a **Modular Monolith** architecture governed by **Domain-Driven Design (DDD)** principles, transitioning into an **Event-Driven Architecture**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Next.js Frontend Client                         │
│             (Interactive Dashboard, Watchlists, AI Chat)               │
└───────────────────▲────────────────────────────────┬───────────────────┘
                    │ Server-Sent Events (SSE)       │ REST API Requests
┌───────────────────┴────────────────────────────────▼───────────────────┐
│                     Spring Boot Application Core                       │
│                                                                        │
│   ┌──────────────┐   ┌──────────────┐   ┌──────────────────────────┐   │
│   │     User     │   │  Watchlist   │   │        MarketData        │   │
│   │    Module    │   │    Module    │   │          Module          │   │
│   └──────┬───────┘   └──────┬───────┘   └────────────┬─────────────┘   │
│          │                  │                        │                 │
│          └──────────────────┴───────────┬────────────┘                 │
│                                         │                              │
│                               ┌─────────▼─────────┐                    │
│                               │   Shared Kernel   │                    │
│                               │  (Events, VOs)    │                    │
│                               └─────────┬─────────┘                    │
│                                         │                              │
│                    Transactional Outbox (BEFORE_COMMIT)                │
│                                         │                              │
│                              ┌──────────▼──────────┐                   │
│                              │     OutboxRelay     │                   │
│                              │ (Polled Dispatcher) │                   │
│                              └──────────┬──────────┘                   │
└─────────────────────────────────────────┼──────────────────────────────┘
                                          │
                                ┌─────────▼─────────┐
                                │   Apache Kafka    │
                                │   (KRaft Mode)    │
                                └─────────┬─────────┘
                                          │
                                ┌─────────▼─────────┐
                                │ Idempotent Kafka  │
                                │ Event Consumers   │
                                └───────────────────┘

         ┌───────────────────┐         ┌────────────────────┐
         │   PostgreSQL 16   │         │      Redis 7       │
         │ (ACID Persistence)│         │ (Multi-Tier Cache) │
         └───────────────────┘         └────────────────────┘
```

---

## 🚀 Key Features & Engineering Highlights

### 1. Event-Driven Architecture with Transactional Outbox
- **Guaranteed At-Least-Once Delivery**: Employs the **Transactional Outbox Pattern** to write domain events (`WatchlistItemAddedEvent`) to an `outbox_events` table within the same database transaction as the entity state.
- **Background Relay**: A dedicated `@Scheduled` `OutboxRelay` processes and transmits batches to **Apache Kafka**.
- **Consumer Idempotency**: Consumers verify incoming event UUIDs against an indexed `processed_events` table before execution, preventing duplicate side-effects.

### 2. High-Performance Multi-Tier Caching & Stampede Protection
- **Two-Tier Cache Hierarchy**:
  - **L1 In-Memory**: Ultra-low-latency `ConcurrentHashMap` with short-lived **90-second TTL**.
  - **L2 Distributed**: **Redis** caching with **5-minute TTL** and automatic L1 backfill upon cache hit.
- **Request Coalescing**: Uses `ConcurrentHashMap` and `CompletableFuture` to coalesce simultaneous requests for the same stock ticker, preventing **cache stampedes** and shielding downstream external APIs.

### 3. Fault-Tolerant External Provider Integration
- **Resilience4j Integration**: Encapsulates external market API invocations within `@CircuitBreaker` and `@RateLimiter` configurations.
- **Automated Multi-Provider Failover**: Automatically fails over from primary **Finnhub** market quotes to fallback **Yahoo Finance** endpoints if latency spikes or rate limits are reached.

### 4. Real-Time Market Data & Adaptive Ingestion
- **Adaptive Scheduler**: Operates NYSE market-hours-aware scheduling (09:30–16:00 EST) with **12-second refresh cycles** that rotate ticker queues in batches of 10.
- **Live Broadcasting**: Employs **Server-Sent Events (SSE)** via `MarketDataBroadcaster` to stream live quote changes (`price-update`) to active frontend clients without polling overhead.

### 5. AI-Powered Market Intelligence (Spring AI + Google Gemini)
- **Context-Aware Prompts**: Dynamically constructs financial context (price, 24h delta, day high/low, trading volume, sector) across active tickers.
- **Structured Insights & Streaming**: Generates market sentiment classifications, structured risk indicators, and streaming chat answers via `ChatClient` with Google Gemini.

### 6. Strict Architectural Boundaries (ArchUnit)
- Structural integrity is verified through automated **ArchUnit** unit tests. Cross-module direct dependencies between `User`, `Watchlist`, `MarketData`, and `AI` are prohibited; all cross-boundary interactions occur strictly via events in the `SharedKernel`.

---

## 🛠 Technology Stack

| Layer | Technologies |
|---|---|
| **Core Framework** | Java 21, Spring Boot 4.1.0, Spring Modulith 2.1.0, Spring Data JPA |
| **Messaging & Broker** | Apache Kafka 3.9.2 (KRaft Mode, Spring Kafka) |
| **Caching & In-Memory** | Redis 7 (Alpine), ConcurrentHashMap L1 Cache |
| **Databases** | PostgreSQL 16 |
| **Fault Tolerance & Resilience** | Resilience4j (CircuitBreaker, RateLimiter, AspectJ) |
| **Artificial Intelligence** | Spring AI 2.0.0, Google GenAI (Gemini) Starter |
| **Real-Time Delivery** | Spring WebMVC Server-Sent Events (SSE), Spring WebSocket |
| **Architecture Enforcement** | ArchUnit 1.2.1 |
| **Frontend** | Next.js, React, TypeScript, Tailwind CSS |
| **Containerization** | Docker, Docker Compose |

---

## 📦 Bounded Contexts & Module Design

The codebase follows Domain-Driven Design (DDD) with clean hexagonal layering:

```
src/main/java/org/workshop/marketcanvas/
├── sharedkernel/               # Ubiquitous domain models & cross-cutting infra
│   ├── domain/                 # Value Objects (UserId, AssetId) & OutboxEvent
│   ├── events/                 # Domain Events (WatchlistItemAddedEvent, StockPriceUpdatedEvent)
│   └── infrastructure/         # OutboxRelay, KafkaTopics, WatchlistOutboxListener
│
├── watchlist/                  # Watchlist lifecycle and management
│   ├── domain/                 # Watchlist Aggregate Root & domain invariants
│   ├── application/            # Watchlist command services
│   └── infrastructure/         # Web REST controllers & JPA repositories
│
├── marketdata/                 # Stock quotes, feeds, and ticker registries
│   ├── domain/                 # StockQuote domain record
│   ├── application/            # ResilientMarketDataService, Kafka consumers
│   └── infrastructure/         # Finnhub/Yahoo adapters, MultiTierMarketDataCache, SSE broadcaster
│
├── ai/                         # Financial analysis engine
│   ├── domain/                 # AnalysisResponse, MarketSentiment, RiskLevel
│   ├── application/            # AiAnalysisService, MarketContextBuilder
│   └── infrastructure/         # AiChatController, prompt templates
│
└── user/                       # User profiles & mock identities
```

---

## 🔄 Event-Driven Data Flow & Outbox Pattern

```
1. Client issues POST /api/v1/watchlists/{id}/assets
   └─► Watchlist Aggregate enforces business rule (max 10 assets for free tier).
2. Watchlist records WatchlistItemAddedEvent via @DomainEvents.
3. Spring Data repository saves entity -> triggers BEFORE_COMMIT phase.
4. WatchlistOutboxListener captures event and saves OutboxEvent entity into database.
5. Database transaction commits (Watchlist state + OutboxEvent stored atomically).
6. OutboxRelay (@Scheduled every 5s) queries unprocessed outbox records.
7. OutboxRelay publishes payload to Kafka topic 'platform.watchlist.events'.
8. WatchlistEventConsumer listens to topic:
   ├─► Checks ProcessedEventRepository by eventId (Idempotency check).
   ├─► If duplicate: skips processing.
   └─► If new: records processed_event, triggers market data ingestion, and updates cache.
```

---

## 📡 REST & Streaming APIs

### Market Data & Quotes
- `GET /api/v1/marketdata/quotes/{ticker}` — Fetch latest real-time stock quote (L1/L2 cache-first).
- `GET /api/v1/marketdata/quotes?tickers=AAPL,MSFT,NVDA` — Batch retrieve stock quotes.
- `GET /api/v1/marketdata/status` — Health and latency state of external providers.
- `GET /api/v1/marketdata/stream` — SSE stream emitting live `price-update` events.

### Asset Discovery
- `GET /api/v1/assets` — Retrieve all tracked assets in the asset registry.
- `GET /api/v1/assets/search?q={query}` — Search US equities by symbol or company name.
- `GET /api/v1/assets/{id}` — Get asset metadata by unique identifier.

### Watchlists
- `POST /api/v1/watchlists` — Create a new watchlist.
- `GET /api/v1/watchlists?ownerId={userId}` — Retrieve all watchlists owned by user.
- `POST /api/v1/watchlists/{id}/assets` — Add an asset symbol (triggers outbox event).
- `DELETE /api/v1/watchlists/{id}/assets/{assetId}` — Remove asset from watchlist.
- `DELETE /api/v1/watchlists/{id}` — Delete entire watchlist.

### AI Market Analyst
- `POST /api/v1/ai/analyze` — Request structured sentiment & risk analysis for specific tickers.
- `GET /api/v1/ai/stream?question={q}&tickers=AAPL,GOOGL` — Stream AI response tokens in real-time.

---

## 🚦 Getting Started

### Prerequisites
- **Java 21** (Temurin / OpenJDK)
- **Maven 3.9+** (or use included `./mvnw`)
- **Docker & Docker Compose**
- **Node.js 18+** (for frontend)
- API Keys:
  - [Finnhub API Key](https://finnhub.io/) (Free tier)
  - [Google Gemini API Key](https://aistudio.google.com/)

---

### 1. Clone & Configure Environment

```bash
git clone https://github.com/YousefFayez20/MarketCanvas.git
cd MarketCanvas
```

Create a `.env` file in the root directory:

```env
GEMINI_API_KEY=your_gemini_api_key_here
FINNHUB_API_KEY=your_finnhub_api_key_here
```

---

### 2. Launch Infrastructure Services (Docker)

Spin up PostgreSQL, Apache Kafka (KRaft), and Redis:

```bash
docker compose up -d postgres kafka redis
```

Verify service status:
- **PostgreSQL**: `localhost:5433` (DB: `investment_platform`, User/Pass: `postgres/postgres`)
- **Kafka**: `localhost:9092`
- **Redis**: `localhost:16379`

---

### 3. Run Backend Application

```bash
./mvnw clean spring-boot:run
```

The Spring Boot backend will start on **`http://localhost:8080`**.

---

### 4. Run Frontend Dashboard

```bash
cd frontend
npm install
npm run dev
```

Open **`http://localhost:3000`** in your browser.

---

### 🐳 Full Docker Stack (Optional)

To spin up the entire platform including backend and frontend in containers:

```bash
docker compose up --build -d
```

---

## 🧪 Architecture Enforcement & Testing

MarketCanvas employs **ArchUnit** to enforce architectural rules as automated unit tests:

```bash
./mvnw test -Dtest=ArchitectureEnforcementTest
```

**Enforced Rules:**
- `User` module may only depend on `SharedKernel`.
- `Watchlist` module may only depend on `SharedKernel`.
- `MarketData` module may only depend on `SharedKernel`.

Any direct cross-module coupling violates CI assertions and fails the build immediately.

---

## ⚙️ Configuration Reference

Key application properties (`application.properties`):

| Property | Default Value | Description |
|---|---|---|
| `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka broker cluster connection |
| `spring.data.redis.host` | `localhost` | Redis caching server hostname |
| `spring.data.redis.port` | `16379` | Redis port mapping |
| `spring.datasource.url` | `jdbc:postgresql://localhost:5433/investment_platform` | Primary PostgreSQL database URL |
| `resilience4j.circuitbreaker.instances.finnhub.sliding-window-size` | `10` | Evaluation window for circuit breaker |
| `resilience4j.ratelimiter.instances.finnhub.limit-for-period` | `30` | Max calls allowed in limit refresh period |

---

## 👤 Author

**Youssef Mikhaeil**
- GitHub: [@YousefFayez20](https://github.com/YousefFayez20)
- LinkedIn: [linkedin.com/in/youssef-mikhaeil](https://www.linkedin.com/in/youssef-mikhaeil-16344a1b8/)
