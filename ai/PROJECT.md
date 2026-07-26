# Project Overview — MarketCanvas

## What We Are Building

**MarketCanvas** is an AI-powered Financial Intelligence Platform. It is a research and monitoring assistant for investors.

### The Core Question

> "What should I pay attention to right now — and why?"

That single question is the product.

## Business Goals

1. Help investors understand markets, validate their own thinking, and surface information they would otherwise miss.
2. Optimize for **decision quality** over trade frequency.
3. Build a sustainable SaaS business with tiered pricing.
4. Serve as a learning vehicle for mastering senior-level backend engineering.

## What the Platform Is NOT

- ❌ A brokerage or trading platform — no order execution
- ❌ A robo-advisor — no automated portfolio management
- ❌ A financial advisor — no personalized investment recommendations
- ❌ A prediction engine — no claims of market forecasting accuracy
- ❌ A high-frequency trading system — no sub-second latency requirements

These constraints are not just legal caution — they are product clarity. The platform focuses on the underserved layer: **understanding and context**.

## Core Product Principles

| Principle | Meaning |
|-----------|---------|
| **Explainable** | Every AI output must show its reasoning and sources. |
| **Trustworthy** | Surface uncertainty. Never present delayed data as real-time. |
| **Personalized** | Context-aware defaults based on user profile and risk tolerance. |
| **Multi-Asset** | Equities, ETFs, gold, real estate, bonds — unified layer. |
| **AI-Assisted, Not AI-Dependent** | If the LLM is unavailable, core features still function. |
| **Compliance-Aware** | Design with regulatory boundaries from day one. |
| **Scalable in Thinking** | Start simple. Make reversible decisions. |

## Target Users

| # | Persona | Description | Willingness to Pay |
|---|---------|-------------|-------------------|
| 1 | Beginner Retail Investor | Overwhelmed by financial news, no signal filter | $5–$15/mo |
| 2 | **Active Investor** ⭐ | Daily monitoring, thesis-driven, 2-3hr/day research | $20–$50/mo |
| 3 | Long-Term Investor | Thesis-driven, quarterly rebalancing | $10–$25/mo |
| 4 | Financial Content Creator | YouTube/Substack analysts needing data | $30–$80/mo |
| 5 | Small RIAs (Financial Advisors) | Managing client portfolios | $100–$300/mo |
| 6 | Investment Clubs | Collaborative research groups | Team pricing |

**Primary MVP persona: The Active Investor (#2)** — highest willingness to pay, daily engagement, clearly defined pain points.

## Major Modules (Bounded Contexts)

| Module | Responsibility |
|--------|---------------|
| `user` | Identity, authentication, subscription, preferences |
| `watchlist` | Asset monitoring, alert configuration, watchlist CRUD |
| `marketdata` | Ingestion, normalization, distribution of market data |
| `research` | AI research workspace, RAG pipeline, document management |
| `thesis` | Investment thesis lifecycle, scoring, evidence tracking |
| `portfolio` | Portfolio analysis, risk calculations, scenario modeling |
| `notification` | Multi-channel alert delivery |
| `journal` | Financial journal, AI pattern analysis |

## MVP Feature Set (4 Features)

| Feature | Priority | Rationale |
|---------|----------|-----------|
| AI Watchlists | HIGH | Core engagement loop. Daily active use driver. |
| AI Research Workspace | HIGH | Highest perceived value. Differentiator. |
| Investment Thesis Tracking | MEDIUM | Unique. Strong retention anchor. |
| AI Financial Journal | MEDIUM | Retention lock-in. Low build cost. |

## Current Maturity

**Stage 2 — Event-Driven System** (Phase 1 IN PROGRESS)

The project has established bounded context boundaries, domain events, the Transactional Outbox Pattern, and a Kafka-based architecture with DLQ and Idempotency. Watchlist REST API exists. **Currently working on the final Month 3-4 tasks:** Market Data producers, Schema Registry (Avro/JSON Schema), and Kafka Connect S3 archival.

## Pricing Model

| Tier | Price | Target |
|------|-------|--------|
| Free — "Discover" | $0/mo | Beginners, students, habit formation |
| Pro — "Analyse" | $25/mo ($200/yr) | Active investors, content creators |
| Team — "Collaborate" | $80/mo per seat | Small RIAs, investment clubs |
