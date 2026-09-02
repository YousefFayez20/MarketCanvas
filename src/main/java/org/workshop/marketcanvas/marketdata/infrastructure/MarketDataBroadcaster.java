package org.workshop.marketcanvas.marketdata.infrastructure;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class MarketDataBroadcaster {
private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    public SseEmitter addEmitter(SseEmitter emitter){
        emitters.add(emitter);
        emitter.onCompletion(() ->{
            log.debug("SSE connection completed, removing emitter");
            this.emitters.remove(emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("SSE connection timed out, removing emitter");
            this.emitters.remove(emitter);
        });
        emitter.onError(throwable -> {
            log.debug("SSE connection error: {}, removing emitter", throwable.getMessage());
            this.emitters.remove(emitter);
        });
        return emitter;
    }
    public void broadcast(StockQuote quote){
        for(SseEmitter emitter: emitters){
            try {
                emitter.send(SseEmitter.event().name("price-update").data(quote));
            }catch (Exception ex){
                log.warn("Failed to send price update to subscriber, removing emitter: {}", ex.getMessage());
                emitters.remove(emitter);
            }
        }
    }

}
