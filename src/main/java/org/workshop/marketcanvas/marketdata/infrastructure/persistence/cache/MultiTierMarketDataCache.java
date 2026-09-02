package org.workshop.marketcanvas.marketdata.infrastructure.persistence.cache;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.tools.cache.CachedClassEntry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class MultiTierMarketDataCache {
    private record CachedEntry(StockQuote quote, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
    private final RedisTemplate<String, StockQuote> redisTemplate;
    private final ConcurrentHashMap<String, CachedEntry> l1Cache = new ConcurrentHashMap<>();
    private static final String KEY_PREFIX = "marketdata:quote:";
    private static final Duration L1_TTL = Duration.ofSeconds(90);
    private static final Duration L2_TTL = Duration.ofMinutes(5);

    public Optional<StockQuote> get(String ticker) {
        // 1. Check L1 In-Memory Cache first
        CachedEntry l1Entry = l1Cache.get(ticker);
        if (l1Entry != null && !l1Entry.isExpired()) {
            log.debug("L1 Cache HIT for {}", ticker);
            return Optional.of(l1Entry.quote());
        }
        // 2. Check L2 Redis Cache
        try {
            StockQuote l2Quote = redisTemplate.opsForValue().get(KEY_PREFIX + ticker);
            if (l2Quote != null) {
                log.debug("L2 Cache HIT for {}. Backfilling L1.", ticker);
                // Backfill L1 so the next read is faster
                l1Cache.put(ticker, new CachedEntry(l2Quote, Instant.now().plus(L1_TTL)));
                return Optional.of(l2Quote);
            }
        } catch (Exception e) {
            // CRITICAL: Never let a Redis failure crash the app. Just log and act like a cache miss.
            log.warn("L2 Cache (Redis) UNAVAILABLE for {}: {}", ticker, e.getMessage());
        }
        log.debug("Cache MISS for {}", ticker);
        return Optional.empty();
    }
    public void put(String ticker, StockQuote quote) {
        // Write to L1
        l1Cache.put(ticker, new CachedEntry(quote, Instant.now().plus(L1_TTL)));

        // Write to L2
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + ticker, quote, L2_TTL);
        } catch (Exception e) {
            log.warn("Failed to write to L2 Cache (Redis) for {}: {}", ticker, e.getMessage());
        }
    }
    public void invalidate(String ticker) {
        l1Cache.remove(ticker);
        try {
            redisTemplate.delete(KEY_PREFIX + ticker);
        } catch (Exception e) {
            log.warn("Failed to invalidate L2 Cache (Redis) for {}", ticker);
        }
    }
}
