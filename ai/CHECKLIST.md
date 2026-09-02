# Development Checklists — MarketCanvas

> Use before every significant development activity.

---

## Before Implementing a New Feature

- [ ] Read `/ai/CURRENT_STATE.md` — is there anything blocking this?
- [ ] Read `/ai/TASKS.md` — is this task documented? What are its dependencies?
- [ ] Read `/ai/ARCHITECTURE.md` — which bounded context does this belong to?
- [ ] Read `/ai/PATTERNS.md` — what conventions apply?
- [ ] Identify the package: `context/domain/`, `context/application/`, or `context/infrastructure/`?
- [ ] Does this require a new domain event? If yes, use past tense naming.
- [ ] Does this require cross-context communication? If yes, use events, not direct imports.
- [ ] Will this need a database table? Plan the schema before coding.
- [ ] Explain the approach to the user before writing code.

---

## Before Creating a REST API Endpoint

- [ ] Define the HTTP method (GET, POST, PUT, DELETE)
- [ ] Define the URL path (`/api/v1/{context}/{resource}`)
- [ ] Define request DTO with validation annotations
- [ ] Define response DTO (never expose domain entities directly)
- [ ] Define error responses (400, 401, 403, 404, 409, 500)
- [ ] Add input validation at the controller layer
- [ ] Route business logic through an Application Service (never put logic in controllers)
- [ ] Add authentication/authorization checks
- [ ] Document in OpenAPI/Swagger

---

## Before Modifying the Database Schema

- [ ] Is `ddl-auto: update` still acceptable, or do we need a Flyway migration?
- [ ] Will this break existing data? Is a data migration needed?
- [ ] Are indexes needed for the new query patterns?
- [ ] Is the table in the correct schema/namespace for its bounded context?
- [ ] Update `/ai/ARCHITECTURE.md` (Database Schema section)

---

## Before Changing Architecture

- [ ] Document the change in `/ai/DECISIONS.md` with full ADR format
- [ ] Explain why the change is needed
- [ ] List alternatives considered
- [ ] State trade-offs explicitly
- [ ] Get user approval before implementing
- [ ] Update `/ai/ARCHITECTURE.md` after implementing
- [ ] Run ArchUnit tests to verify boundary compliance

---

## Before Merging / Completing Work

- [ ] Run `./mvnw clean compile` — BUILD SUCCESS?
- [ ] Run `./mvnw test` — all tests pass?
- [ ] Are ArchUnit boundaries respected?
- [ ] Is there dead code to remove?
- [ ] Are there TODO comments that should be tracked in TASKS.md?
- [ ] Update `/ai/CURRENT_STATE.md`
- [ ] Update `/ai/TASKS.md`
- [ ] Append `/ai/SESSION_LOG.md`

---

## Before Refactoring

- [ ] What is the motivation? (readability, performance, correctness)
- [ ] Does the refactoring change behavior? If yes, add tests first.
- [ ] Does it respect module boundaries?
- [ ] Does it follow `/ai/PATTERNS.md`?
- [ ] Are all existing tests still passing?
- [ ] Is the refactoring scope small enough to review?

---

## Before Adding a New Dependency

- [ ] What problem does it solve?
- [ ] Is it maintained? (check last release date, GitHub stars, issue response time)
- [ ] Is it compatible with our Spring Boot version?
- [ ] Does it introduce license concerns?
- [ ] Can we solve this without the dependency?
- [ ] Document in `/ai/STACK.md`
- [ ] Document in `/ai/DECISIONS.md` if it's a significant choice

---

## Before Deploying (Future)

- [ ] All tests pass
- [ ] Flyway migrations are tested
- [ ] Environment variables are configured
- [ ] Secrets are not hardcoded
- [ ] Health check endpoints are verified
- [ ] Logging and monitoring are configured
- [ ] Rollback plan is documented
- [ ] Database backup has been taken

---

## Before Adding a New Bounded Context

- [ ] Is the context boundary clear? Does it have its own ubiquitous language?
- [ ] Create package structure: `domain/`, `application/`, `infrastructure/`
- [ ] Add to ArchUnit test: `whereLayer("[Name]").mayOnlyAccessLayers("SharedKernel")`
- [ ] Define the domain events it will publish and consume
- [ ] Update `/ai/ARCHITECTURE.md`
- [ ] Update `/ai/GLOSSARY.md` with new domain terms

---

## Before Writing a Kafka Consumer

- [ ] Define the topic and consumer group ID (using `KafkaTopics` constants)
- [ ] Implement idempotency (`ProcessedEvent` table)
- [ ] Use `@Transactional` to share transaction between idempotency check and business logic
- [ ] Rely on `KafkaConsumerConfig` (`DefaultErrorHandler`) for retries and DLQ routing
- [ ] Update `/ai/ARCHITECTURE.md` (Kafka Topics section)

---

## Before Modifying Cache Layer

- [ ] Ensure TTLs are configured and appropriate for the data volatility.
- [ ] Implement graceful degradation (e.g., fallback to DB or L1 if Redis is down).
- [ ] Use stampede protection (`computeIfAbsent`) for expensive queries.
- [ ] Ensure serializability of cache payloads (e.g., Jackson JSR310 module for `Instant` or `LocalDateTime`).

---

## Before Creating SSE Endpoints

- [ ] Use `SseEmitter` with appropriate timeouts.
- [ ] Manage emitter lifecycle (onCompletion, onTimeout, onError) to avoid memory leaks.
- [ ] Use thread-safe collections (e.g., `CopyOnWriteArrayList`) for the emitter registry.
- [ ] Keep payload size minimal to reduce bandwidth over open connections.
