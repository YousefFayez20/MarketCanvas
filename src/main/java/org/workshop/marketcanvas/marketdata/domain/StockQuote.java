package org.workshop.marketcanvas.marketdata.domain;

import org.workshop.marketcanvas.sharedkernel.events.StockPriceUpdatedEvent;

import java.math.BigDecimal;
import java.time.Instant;

public record StockQuote(String ticker,
                         BigDecimal price,
                         BigDecimal change24h,
                         BigDecimal changePercent24h,
                         BigDecimal high24h,
                         BigDecimal low24h,
                         BigDecimal openPrice,
                         BigDecimal previousClose,
                         Long volume,
                         Instant lastUpdated,
                         String providerSource) {
    public StockQuote {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be null or blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        if (lastUpdated == null) {
            lastUpdated = Instant.now();
        }
        // Normalize ticker casing to avoid partition mismatches ('aapl' vs 'AAPL')
        ticker = ticker.toUpperCase().trim();
    }
    public StockPriceUpdatedEvent toEvent() {
        return new StockPriceUpdatedEvent(
                ticker,
                price,
                change24h,
                changePercent24h,
                high24h,
                low24h,
                openPrice,
                previousClose,
                volume,
                lastUpdated,
                providerSource
        );
    }
}
