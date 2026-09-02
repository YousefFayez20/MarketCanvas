# Roadmap — MarketCanvas

> Organized by the 12-Month Learning Roadmap from the Technical Blueprint.
>
> **Convention: Always Shippable.** After every feature milestone, the app must be testable end-to-end through its UI — not just through `curl` commands. Frontend work is inserted between backend milestones as needed.

---

## Phase 1: Foundation (Month 1–4) — **IN PROGRESS**

### Month 1–2: Domain-Driven Design & Modular Monolith ✅ COMPLETE

- [x] Bounded context identification
- [x] Package-by-domain structure
- [x] Value objects (`UserId`, `AssetId`)
- [x] Watchlist Aggregate Root with business invariants
- [x] Domain events (`@DomainEvents` / `@AfterDomainEventPublication`)
- [x] ArchUnit architectural enforcement tests
- [x] Factory method pattern for aggregate creation

### Month 3–4: Event-Driven Architecture with Kafka 🔄 IN PROGRESS

- [x] Docker Compose with Kafka (KRaft) and PostgreSQL
- [x] Transactional Outbox Pattern (entity + listener + relay)
- [x] Kafka producer via `KafkaTemplate`
- [x] Idempotent Kafka consumer with `ProcessedEvent` table
- [x] Two-tier exception handling (poison pill vs transient)
- [x] **Fix payload schema mismatch** (eventId not in Kafka message)
- [x] **Fix WatchlistTest** (constructor access + assertion message)
- [x] **Delete Stage 1 listener** (`MarketDataWatchlistListener`)
- [x] Build Watchlist REST API (Controller + Application Service)
- [x] Make Watchlist a JPA entity
- [x] End-to-end test: HTTP → Aggregate → Outbox → Kafka → Consumer
- [x] Dead Letter Queue (DLQ) for failed messages
- [x] Kafka topic constants class

### MVP Sprint: Full-Stack Watchlist App ✅ COMPLETE
- [x] **Complete Watchlist CRUD API** (GET, DELETE endpoints, response DTOs, CORS)
- [x] **Asset Registry** (static list of 50 real US stocks served via search endpoint)
- [x] **Mock User Registry** (3 demo users: Alice, Bob, Carol served via API)
- [x] **Next.js Frontend** (Dashboard, Watchlist Detail, Market Directory, In-App API Test Bench)
- [x] **End-to-end verification through UI** (create watchlist, search/add/remove real assets, capacity enforcement)
- [x] **Unified Multi-stage Docker Compose** (PostgreSQL, Kafka KRaft, Spring Boot backend, Next.js frontend)

### Month 3–4 Remaining: Advanced Kafka Infrastructure 🔄 IN PROGRESS
- [x] **Real Market Data Ingestion & Snapshot Pipeline** (TASK-017)
  - Pluggable provider adapter: Finnhub.io (60 req/min) + Yahoo Finance fallback
  - Adaptive batch polling (10 tickers/batch) during NYSE hours
  - Multi-tier cache (L1 ConcurrentHashMap, L2 Redis) with stampede protection
  - Ingest, cache in PostgreSQL (`asset_quotes`), and publish `StockPriceUpdatedEvent` to `platform.marketdata.prices`
  - *Next.js UI SSE integration remaining*
- [ ] **Event schema design with Avro / Schema Registry** (TASK-018)
- [ ] **Set up Kafka Connect to archive events to S3 / MinIO** (TASK-019)

---

## Phase 2: AI Engineering (Month 5–8)

### Month 5–6: AI Engineering & RAG Systems

- [ ] Document ingestion pipeline (SEC 10-K filings)
- [ ] Text extraction and chunking strategy
- [ ] Embedding generation with embedding model
- [ ] pgvector setup for vector similarity search
- [ ] Research Workspace with basic RAG (retrieval + generation)
- [ ] Source citation in every LLM response
- [ ] Evaluation dataset (50 Q&A pairs)
- [ ] RAG evaluation metrics (faithfulness, relevance, recall)

### Month 7–8: Distributed Systems & Reliability

- [ ] Saga pattern for cross-context workflows
- [x] Resilience4j circuit breakers for external API calls
- [ ] Failure scenario simulation
- [ ] Distributed tracing with OpenTelemetry
- [x] Redis integration for caching

---

## Phase 3: Production Readiness (Month 9–12)

### Month 9–10: Observability, Performance & Production Readiness

- [ ] Micrometer metrics + Prometheus endpoints
- [ ] Grafana dashboards (four golden signals)
- [ ] Load testing (1,000 concurrent users with k6)
- [ ] Database query optimization (EXPLAIN ANALYZE)
- [ ] Structured JSON logging with correlation IDs
- [ ] Flyway database migrations (replace `ddl-auto: update`)

### Month 11–12: Launch, SaaS Operations & Growth

- [ ] 50 beta users launch
- [ ] Stripe subscription integration
- [ ] Product analytics (PostHog or Mixpanel)
- [ ] 10 user interviews
- [ ] Feature flagging
- [ ] A/B testing framework

---

## Future Ideas (Post-12-Month)

- Portfolio Impact Simulator (historical scenario analysis)
- Multi-Asset Intelligence (cross-asset correlation)
- Personalized Risk Guardrails
- Event Timeline (chronological market + user events)
- Opportunity Discovery Engine (theme emergence, sector flow)
- Agentic AI workflows (multi-step research with tool calling)
- Mobile app (React Native or Flutter)
- International market data (LSE, TSE, SSE)
- Debezium CDC to replace Outbox polling

---

## Stretch Goals

- [x] Real-time WebSocket / SSE price streaming
- [ ] Bloomberg-lite terminal UI for power users
- [ ] Natural language alert rules ("Alert me when any CEO sells >$1M")
- [ ] Investment community features (shared watchlists, group journals)
- [ ] API marketplace for developer integrations
