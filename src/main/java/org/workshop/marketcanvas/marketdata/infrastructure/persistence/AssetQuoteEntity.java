package org.workshop.marketcanvas.marketdata.infrastructure.persistence;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "asset_quotes")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA requires no-arg, but callers use fromDomain()
@AllArgsConstructor
@Builder
public class AssetQuoteEntity {
    @Id
    @Column(name = "ticker", length = 10, nullable = false)
    String ticker;
    @Column(name = "price", precision = 19, scale = 4, nullable = false)
    private BigDecimal price;
    @Column(name = "change_24h", precision = 19, scale = 4)
     private BigDecimal change24h;
    @Column(name = "change_percent_24h", precision = 10, scale = 4)
    private BigDecimal changePercent24h;
    @Column(name = "high_24h", precision = 19, scale = 4)
    private BigDecimal high24h;
    @Column(name = "low_24h", precision = 19, scale = 4)
    private BigDecimal low24h;
    @Column(name = "open_price", precision = 19, scale = 4)
    private BigDecimal openPrice;
    @Column(name = "previous_close", precision = 19, scale = 4)
    private BigDecimal previousClose;
    @Column(name = "volume")
    private Long volume;
    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;
    @Column(name = "provider_source", length = 50)
    private String providerSource;
    public StockQuote toDomain() {
        return new StockQuote(
                this.getTicker(),
                this.getPrice(),
                this.getChange24h(),
                this.getChangePercent24h(),
                this.getHigh24h(),
                this.getLow24h(),
                this.getOpenPrice(),
                this.getPreviousClose(),
                this.getVolume(),
                this.getLastUpdated(),
                this.getProviderSource()
        );
        }
    public static AssetQuoteEntity fromDomain(StockQuote quote) {
        return new AssetQuoteEntity(quote.ticker(),
                quote.price(),
                quote.change24h(),
                quote.changePercent24h(),
                quote.high24h(),
                quote.low24h(),
                quote.openPrice(),
                quote.previousClose(),
                quote.volume(),
                quote.lastUpdated(),
                quote.providerSource());
        /* use @Builder or constructor */ }
}
