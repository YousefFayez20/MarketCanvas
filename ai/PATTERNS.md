# Coding Patterns & Conventions — MarketCanvas

> Document every repeatable pattern. If you do it twice, it becomes a convention.

---

## Package Structure

Every bounded context follows this internal structure:

```
context-name/
├── application/          ← Use cases, event listeners, DTOs
│   └── messaging/        ← Kafka consumers (if applicable)
├── domain/               ← Aggregates, entities, value objects, domain services
│   └── events/           ← Domain events (if context-specific)
└── infrastructure/       ← JPA repositories, REST controllers, external adapters
    └── messaging/        ← Kafka producers, idempotency entities (if applicable)
```

### Rules:
- `domain/` has **zero** Spring annotations (except `@Entity` for JPA). Pure Java.
- `application/` contains `@Component`, `@Service`, `@TransactionalEventListener`.
- `infrastructure/` contains `@Repository`, `@RestController`, adapters to external systems.
- No class in one context may import a class from another context (enforced by ArchUnit).

---

## Value Objects

Use Java `record` types for identity and measurement values.

```java
public record UserId(UUID value) {
    public UserId {
        if (value == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
    }
    public static UserId generate() {
        return new UserId(UUID.randomUUID());
    }
}
```

### Rules:
- Always validate in the compact constructor.
- Provide a static factory method for generation.
- Use these in domain code for type safety. Use `UUID` in events and API contracts for serialization simplicity.

---

## Aggregate Roots

```java
public class Watchlist {
    // Private constructor — enforce creation via factory method
    private Watchlist(UUID id, UserId ownerId, String name) { ... }

    // Public factory method — the only way to create
    public static Watchlist create(UserId ownerId, String name) { ... }

    // Business methods enforce invariants
    public void addAsset(AssetId assetId) {
        // validate
        // enforce business rules
        // mutate state
        // queue domain event
    }
}
```

### Rules:
- Constructor is **private**.
- Creation goes through a **static factory method**.
- All business rules are enforced **inside** the aggregate. Never in a service.
- Domain events are queued inside the aggregate and published via `@DomainEvents`.
- No setters. State changes happen through named behavior methods.

---

## Domain Events

```java
public record WatchlistItemAddedEvent(
    UUID watchlistId,
    UUID assetId,        // Use primitives for wire format
    Instant timestamp
) {}
```

### Rules:
- Always named in **past tense** (something that happened).
- Use **primitive types** (UUID, String, Instant) — not domain objects — for events that cross module boundaries.
- Events are facts. They are immutable. They are never modified after creation.

---

## JPA Entities

```java
@Entity
@Table(name = "table_name")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA requires no-arg constructor
@AllArgsConstructor
public class SomeEntity {
    @Id
    private UUID id;
    // fields...
}
```

### Rules:
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` — JPA needs it, but callers shouldn't use it.
- Use `@Getter` from Lombok. Avoid `@Setter` unless explicitly needed (prefer domain methods).
- Initialize defaults inline: `private boolean processed = false;`

---

## Outbox Pattern

```
Domain Event → @TransactionalEventListener(BEFORE_COMMIT)
            → Save OutboxEvent to DB (same transaction)
            → @Scheduled relay polls DB every 5 seconds
            → Sends to Kafka via KafkaTemplate
            → Marks OutboxEvent as processed
```

### Rules:
- The listener runs in `BEFORE_COMMIT` phase to share the transaction.
- The relay uses `.get()` on the Kafka future (blocking) to confirm delivery before marking processed.
- Batch processing: `findTop100ByProcessedFalseOrderByCreatedAt()`.

---

## Kafka Consumers

```java
@KafkaListener(topics = KafkaTopics.TOPIC_NAME, groupId = "consumer-group")
@Transactional
public void consume(ConsumerRecord<String, String> record) {
    // 1. Deserialize
    // 2. Idempotency check (ProcessedEvent table)
    // 3. Business logic
    // 4. Save idempotency marker
    
    // Note: No try-catch blocks needed. Spring Kafka's DefaultErrorHandler 
    // will handle exceptions, retries, and DLQ routing automatically.
}
```

### Rules:
- `@Transactional` wraps the entire method. Idempotency marker and business logic share one transaction.
- Exceptions are handled globally by `KafkaConsumerConfig` (DefaultErrorHandler + DLQ). Do not use `try-catch` to swallow or manually route errors.
- Every consumer MUST have an idempotency mechanism.

---

## Error Handling

- **Domain exceptions:** `IllegalArgumentException` for invalid input, `IllegalStateException` for business rule violations.
- **Infrastructure exceptions:** Wrapped in `RuntimeException` with descriptive message.
- **Never catch and silently swallow** in production paths. Log at minimum.

---

## Testing Conventions

| Test Type | Location | Purpose | Naming |
|-----------|----------|---------|--------|
| Unit tests | Same package, `src/test` | Domain logic, aggregates | `ClassNameTest` |
| Architecture tests | Root test package | ArchUnit boundary enforcement | `ArchitectureEnforcementTest` |
| Integration tests | TBD | JPA repositories, Kafka consumers | `ClassNameIT` |

### Rules:
- Test aggregates by calling their **public API** (factory methods + behavior methods). Never access private state.
- Use `@SpringBootTest` only for integration tests. Unit tests should be plain JUnit 5.

---

## Logging

- Use `@Slf4j` (Lombok).
- Log at **boundaries**: request in, response out, event received, event published.
- Always include identifiers: `eventId`, `assetId`, `userId`.
- Log format: `log.info("Action description: [{}]", identifier)`.

---

## Configuration

- Use `application.yaml` (not `.properties`). Single file.
- Never hardcode credentials. Use environment variables or secrets management.
- `spring.jpa.open-in-view: false` — always.
