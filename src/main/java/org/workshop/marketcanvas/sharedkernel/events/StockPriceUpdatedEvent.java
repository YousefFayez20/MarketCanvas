package org.workshop.marketcanvas.sharedkernel.events;
import java.math.BigDecimal;
import java.time.Instant;

public record StockPriceUpdatedEvent(
        String ticker,
        BigDecimal price,
        BigDecimal change24h,
        BigDecimal changePercent24h,
        BigDecimal high24h,
        BigDecimal low24h,
        BigDecimal openPrice,
        BigDecimal previousClose,
        Long volume,
        Instant timestamp,
        String providerSource
) {
    public StockPriceUpdatedEvent {
        if (ticker == null || ticker.isBlank()) {
            throw new IllegalArgumentException("Ticker cannot be null or blank");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        // Normalize ticker casing to avoid partition mismatches ('aapl' vs 'AAPL')
        ticker = ticker.toUpperCase().trim();
    }

}
