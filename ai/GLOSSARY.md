# Glossary — MarketCanvas

> Domain-specific terminology. AI assistants should understand these immediately.

---

## Architecture Terms

| Term | Definition |
|------|-----------|
| **Bounded Context** | A strict boundary within which a particular domain model is defined. E.g., `User` in Identity context = security principal. `User` in Watchlist context = just an ID. They are not the same entity. |
| **Aggregate Root** | A cluster of domain objects treated as a single unit. External code can only interact through the root. E.g., `Watchlist` is the root; `WatchlistItem`s are internal. |
| **Domain Event** | An immutable record of something that happened in the domain. Always past tense. E.g., `WatchlistItemAddedEvent`. |
| **Shared Kernel** | A small set of common types (value objects, event interfaces) that all bounded contexts may depend on. |
| **Modular Monolith** | A single deployable application where internal modules are strictly isolated via package boundaries and events. |
| **Outbox Pattern** | A technique to atomically persist business state and an event in the same database transaction. A relay process later publishes the event to the message broker. |
| **Outbox Relay** | The `@Scheduled` component that polls the `outbox_events` table and publishes unprocessed events to Kafka. |
| **Idempotent Consumer** | A Kafka consumer that produces the same result regardless of how many times the same message is delivered. |
| **Poison Pill** | A malformed message that can never be successfully processed. Must be skipped to avoid blocking the partition. |
| **Dead Letter Queue (DLQ)** | A Kafka topic where failed messages are routed after exhausting retries. |
| **Dual-Write Problem** | The impossibility of atomically writing to two different systems (e.g., DB + Kafka). The Outbox Pattern solves this. |

## Product Terms

| Term | Definition |
|------|-----------|
| **Watchlist** | A user-curated list of financial assets they want to monitor. Max 10 items for free tier. |
| **Investment Thesis** | A structured document explaining why a user holds a position, including core assumptions, invalidation criteria, and a time horizon. |
| **Thesis Score** | An AI-generated 0–100 score indicating how well current evidence supports the original investment thesis. |
| **Morning Brief** | A daily AI-generated summary of overnight developments relevant to a user's watchlist. |
| **Research Workspace** | An AI-powered interface where users ask natural language questions and receive answers grounded in retrieved documents (RAG). |
| **Financial Journal** | A structured diary where users record investment decisions, assumptions, and lessons learned. |
| **Signal vs Noise** | The core product problem: helping investors distinguish important information (signal) from irrelevant information (noise). |

## Business Terms

| Term | Definition |
|------|-----------|
| **PMF (Product-Market Fit)** | The point where users are returning daily and willing to pay. Target: Week-4 retention > 45%. |
| **MRR (Monthly Recurring Revenue)** | Total monthly subscription revenue. Target: 5,000 Pro subscribers = $125K MRR. |
| **Active Investor** | Primary MVP persona. Daily monitoring, thesis-driven, 2-3hr/day research, $20-50/mo willingness to pay. |
| **Free → Pro Conversion** | Target: >8% of free users convert to paid. |
| **Retention Loop** | A product mechanism that drives repeated engagement: Morning Brief → Platform Visit → Watchlist Check → Research Query. |

## Technical Abbreviations

| Abbreviation | Full Term |
|-------------|-----------|
| DDD | Domain-Driven Design |
| EDA | Event-Driven Architecture |
| RAG | Retrieval-Augmented Generation |
| CQRS | Command Query Responsibility Segregation |
| ADR | Architecture Decision Record |
| JPA | Java Persistence API |
| CDC | Change Data Capture (Debezium) |
| DLQ | Dead Letter Queue |
| LLM | Large Language Model |
| SLA | Service Level Agreement |
| RBAC | Role-Based Access Control |
| OIDC | OpenID Connect |
| WAL | Write-Ahead Log (PostgreSQL) |

## Asset Classes

| Term | Definition |
|------|-----------|
| **Equities** | Individual stocks (e.g., AAPL, NVDA) |
| **ETFs** | Exchange-Traded Funds (e.g., SPY, QQQ) |
| **XAU/USD** | Gold spot price in US Dollars |
| **TIPS** | Treasury Inflation-Protected Securities |
| **REITs** | Real Estate Investment Trusts |
| **10-K** | Annual report filed with SEC |
| **10-Q** | Quarterly report filed with SEC |
| **8-K** | Current report for major events filed with SEC |
| **EDGAR** | SEC's Electronic Data Gathering, Analysis, and Retrieval system |
