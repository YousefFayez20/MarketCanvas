package org.workshop.marketcanvas.sharedkernel.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import tools.jackson.core.JacksonException;

/**
 * Kafka consumer error handling configuration.
 *
 * <p>Configures how Spring Kafka handles exceptions thrown by {@code @KafkaListener} methods:</p>
 * <ul>
 *   <li>3 retry attempts with 1-second fixed backoff for transient errors</li>
 *   <li>Immediate DLQ routing (no retries) for poison pill exceptions</li>
 *   <li>Dead Letter Queue publishing when retries are exhausted</li>
 * </ul>
 */
@Configuration
@Slf4j
public class KafkaConsumerConfig {

    @Bean
    public CommonErrorHandler commonErrorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        // Step 1: Create the DLQ recoverer
        // When all retries are exhausted, publish the failed message to the DLQ topic.
        // The original message + exception details are preserved in Kafka headers.
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(
                        KafkaTopics.WATCHLIST_EVENTS_DLQ,
                        record.partition()
                )
        );

        // Step 2: Create the error handler with backoff strategy
        // FixedBackOff(intervalMs, maxAttempts)
        //   - 1000ms = wait 1 second between retries
        //   - 3 = maximum 3 retry attempts
        // Total processing attempts = 1 (initial) + 3 (retries) = 4
        // Total time before DLQ = ~3 seconds (3 retries × 1 second)
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                recoverer,
                new FixedBackOff(1000L, 3L)
        );

        // Step 3: Configure non-retryable exceptions (poison pills)
        // These exceptions mean the message is fundamentally broken — retrying
        // will never fix it. Send directly to DLQ without wasting time on retries.
        //
        // JacksonException   → malformed JSON that can never be parsed
        // IllegalArgumentException → invalid UUID format, null value objects
        // IllegalStateException    → business rule violations (invariant breaks)
        errorHandler.addNotRetryableExceptions(
                JacksonException.class,
                IllegalArgumentException.class,
                IllegalStateException.class
        );

        log.info("Kafka consumer error handler configured: " +
                        "3 retries, 1s backoff, DLQ topic: {}",
                KafkaTopics.WATCHLIST_EVENTS_DLQ);

        return errorHandler;
    }
}

