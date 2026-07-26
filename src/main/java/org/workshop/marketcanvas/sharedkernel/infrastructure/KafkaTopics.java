package org.workshop.marketcanvas.sharedkernel.infrastructure;

public final class KafkaTopics {
    private KafkaTopics(){

    }
    public static final String WATCHLIST_EVENTS_DLQ = "platform.watchlist.events.dlq";
    public static final String WATCHLIST_EVENTS = "platform.watchlist.events";
}
