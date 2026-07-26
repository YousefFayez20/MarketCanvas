package org.workshop.marketcanvas.sharedkernel.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OutboxEvent {

    @Id
    private UUID id;

    private String aggregateType;

    private String aggregateId;
    private String eventType;

    @Lob
    private String payload;

    private Instant createdAt = Instant.now();

    private boolean processed = false;

}
