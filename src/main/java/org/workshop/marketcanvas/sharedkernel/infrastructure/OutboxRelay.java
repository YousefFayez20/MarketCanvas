package org.workshop.marketcanvas.sharedkernel.infrastructure;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.workshop.marketcanvas.sharedkernel.domain.OutboxEvent;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxRelay {


    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)

    @Transactional

    public void publish() throws Exception {

        List<OutboxEvent> events =
                repository.findTop100ByProcessedFalseOrderByCreatedAt();

        for (OutboxEvent event : events) {
            ObjectNode envelope = objectMapper.createObjectNode();
            envelope.put("eventId",event.getId().toString());
            envelope.put("payload",event.getPayload());

            String kafkaMessage = objectMapper.writeValueAsString(envelope);
            kafkaTemplate
                    .send(
                            KafkaTopics.WATCHLIST_EVENTS,
                            kafkaMessage)
                    .get();

            event.setProcessed(true);
        }

    }

}