package org.workshop.marketcanvas.sharedkernel.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.workshop.marketcanvas.sharedkernel.domain.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByProcessedFalseOrderByCreatedAt();
}