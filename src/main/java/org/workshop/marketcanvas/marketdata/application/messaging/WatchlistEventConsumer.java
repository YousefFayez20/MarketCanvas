package org.workshop.marketcanvas.marketdata.application.messaging;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.workshop.marketcanvas.marketdata.infrastructure.messaging.ProcessedEvent;
import org.workshop.marketcanvas.marketdata.infrastructure.messaging.ProcessedEventRepository;
import org.workshop.marketcanvas.sharedkernel.infrastructure.KafkaTopics;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WatchlistEventConsumer {
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.WATCHLIST_EVENTS, groupId = "market-data-group")
    @Transactional
    public void consume(ConsumerRecord<String,String> record){

            JsonNode envelope = objectMapper.readTree(record.value());
            UUID eventId = UUID.fromString(envelope.get("eventId").asText());
            
            // The actual domain event payload is nested as a string inside the envelope
            JsonNode payload = objectMapper.readTree(envelope.get("payload").asText());
            String assetId = payload.get("assetId").asText();
            if(processedEventRepository.existsById(eventId)){
                log.info("Idempotency hit: Ignored duplicate event [{}]", eventId);
                return;
            }
            log.info("Market Data reacting to new watchlist asset: [{}]. Initializing data fetch...", assetId);
            processedEventRepository.save(new ProcessedEvent(eventId, Instant.now()));
            log.info("Successfully processed and recorded event [{}]", eventId);
        
    }
}
