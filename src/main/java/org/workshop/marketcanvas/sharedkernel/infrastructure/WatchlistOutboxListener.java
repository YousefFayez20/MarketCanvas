package org.workshop.marketcanvas.sharedkernel.infrastructure;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.workshop.marketcanvas.sharedkernel.domain.OutboxEvent;
import org.workshop.marketcanvas.sharedkernel.events.WatchlistItemAddedEvent;
import tools.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WatchlistOutboxListener {
    private final ObjectMapper mapper;
    private final OutboxEventRepository repository;

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void on(WatchlistItemAddedEvent event)
    {
        try{
            OutboxEvent outboxEvent = new OutboxEvent(
                    UUID.randomUUID(),
                    "Watchlist",
                    event.watchlistId().toString(),
                    event.getClass().getSimpleName(),
                    mapper.writeValueAsString(event),
                    Instant.now(),
                    false
            );
            repository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException("System error: Failed to serialize domain event", e);
        }

    }
}
