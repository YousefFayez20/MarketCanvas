# Technology Stack — MarketCanvas

> Every technology used in the project, with version and justification.

---

## Runtime

| Technology | Version | Purpose | Justification |
|-----------|---------|---------|---------------|
| Java | 21 (LTS) | Primary language | Long-term support, virtual threads, pattern matching, records |
| Spring Boot | 4.1.0 | Application framework | Industry standard for Java backend, extensive ecosystem |
| Spring Data JPA | (managed by Boot) | ORM / Data access | Simplifies PostgreSQL interaction, `@DomainEvents` support |
| Spring Security | (managed by Boot) | Authentication/Authorization | OAuth2/OIDC support (not yet configured) |
| Spring Kafka | (managed by Boot) | Kafka integration | `@KafkaListener`, `KafkaTemplate`, consumer group management |
| Spring Modulith | 2.1.0 | Module boundary support | Architectural enforcement, event externalization (future) |
| Lombok | (managed by Boot) | Boilerplate reduction | `@Slf4j`, `@Getter`, `@RequiredArgsConstructor` |
| Jackson 3.x | (managed by Boot) | JSON serialization | `tools.jackson.databind.ObjectMapper` (note: new namespace in Boot 4.x) |

## Data Stores

| Technology | Version | Purpose | Justification |
|-----------|---------|---------|---------------|
| PostgreSQL | 16 | Primary relational database | ACID, pgvector extension, TimescaleDB extension |
| pgvector | (planned) | Vector similarity search | RAG pipeline for AI Research Workspace |
| TimescaleDB | (planned) | Time-series data | Price history, portfolio value over time |
| Redis | (planned) | Caching, sessions | Rate limiting, computed views, session state |

## Messaging

| Technology | Version | Purpose | Justification |
|-----------|---------|---------|---------------|
| Apache Kafka | Latest (Bitnami) | Event streaming | Durable, replayable log. Audit trail. Decoupled communication. |
| KRaft mode | — | Kafka consensus | No Zookeeper dependency. Modern standard. |

## Infrastructure

| Technology | Version | Purpose |
|-----------|---------|---------|
| Docker | Latest | Containerization |
| Docker Compose | 3.9 | Local development orchestration |

## Testing

| Technology | Version | Purpose |
|-----------|---------|---------|
| JUnit 5 | (managed by Boot) | Unit and integration testing |
| ArchUnit | 1.2.1 | Architectural boundary enforcement |
| Spring Boot Test | (managed by Boot) | Integration test support |

## Planned (Not Yet Integrated)

| Technology | Phase | Purpose |
|-----------|-------|---------|
| Flyway | Phase 3 | Database migration management |
| Resilience4j | Phase 2 | Circuit breakers for external APIs |
| OpenTelemetry | Phase 3 | Distributed tracing |
| Prometheus + Grafana | Phase 3 | Metrics and dashboards |
| k6 | Phase 3 | Load testing |
| Stripe | Phase 3 | Payment processing |
| PostHog | Phase 3 | Product analytics |

## Frontend

| Technology | Version | Purpose | Justification |
|-----------|---------|---------|---------------|
| Next.js | Latest (App Router) | Frontend framework | React-based, SSR/SSG capable, TypeScript-first, planned in ARCHITECTURE.md |
| TypeScript | Latest | Type safety | Catches bugs at compile time, better DX |

## Build Tools

| Tool | Purpose |
|------|---------|
| Maven (mvnw) | Build and dependency management |
| maven-compiler-plugin | Java 21 compilation with Lombok annotation processing |
| spring-boot-maven-plugin | Executable JAR packaging |

## Important Notes

- **Jackson 3.x namespace change:** Spring Boot 4.x uses `tools.jackson.databind.ObjectMapper` instead of the older `com.fasterxml.jackson.databind.ObjectMapper`. The `writeValueAsString()` method now throws checked exceptions.
- **`open-in-view: false`:** Explicitly disabled to prevent the Open Session in View anti-pattern.
- **`hibernate.ddl-auto: update`:** Acceptable for development. Must be replaced with Flyway before production.
