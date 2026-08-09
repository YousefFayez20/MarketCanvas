package org.workshop.marketcanvas.marketdata.infrastructure.adapters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.workshop.marketcanvas.marketdata.domain.MarketDataProvider;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class YahooFinanceMarketDataProvider implements MarketDataProvider {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public YahooFinanceMarketDataProvider(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;

        this.restClient = restClientBuilder
                .baseUrl("https://query1.finance.yahoo.com/v8/finance/chart")
                .defaultHeader(
                        HttpHeaders.USER_AGENT,
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
                )
                .build();
    }

    @Override
    public Optional<StockQuote> fetchQuote(String ticker) {
        try {
            String responseBody = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{ticker}")
                            .queryParam("interval", "1d")
                            .queryParam("range", "1d")
                            .build(ticker))
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode result = root
                    .path("chart")
                    .path("result")
                    .path(0);

            if (result.isMissingNode() || result.isNull()) {
                return Optional.empty();
            }

            JsonNode meta = result.path("meta");

            if (meta.isMissingNode() || meta.isNull()) {
                return Optional.empty();
            }

            JsonNode priceNode = meta.path("regularMarketPrice");
            JsonNode previousCloseNode = meta.path("chartPreviousClose");

            if (priceNode.isMissingNode()
                    || priceNode.isNull()
                    || previousCloseNode.isMissingNode()
                    || previousCloseNode.isNull()) {
                return Optional.empty();
            }

            BigDecimal price = priceNode.decimalValue();
            BigDecimal previousClose = previousCloseNode.decimalValue();

            if (price.compareTo(BigDecimal.ZERO) <= 0
                    || previousClose.compareTo(BigDecimal.ZERO) <= 0) {
                return Optional.empty();
            }

            BigDecimal change24h = price.subtract(previousClose);

            BigDecimal changePercent24h = change24h
                    .divide(previousClose, 4, RoundingMode.HALF_EVEN)
                    .multiply(BigDecimal.valueOf(100));

            BigDecimal high24h =
                    meta.path("regularMarketDayHigh").decimalValue();

            BigDecimal low24h =
                    meta.path("regularMarketDayLow").decimalValue();

            Long volume =
                    meta.path("regularMarketVolume").longValue();

            StockQuote quote = new StockQuote(
                    ticker,
                    price,
                    change24h,
                    changePercent24h,
                    high24h,
                    low24h,
                    null,
                    previousClose,
                    volume,
                    Instant.now(),
                    "YAHOO_FINANCE"
            );

            return Optional.of(quote);

        } catch (Exception e) {
            log.error(
                    "Failed to fetch Yahoo Finance quote for ticker {}",
                    ticker,
                    e
            );
            return Optional.empty();
        }
    }

    @Override
    public Map<String, StockQuote> fetchBatchQuotes(List<String> tickers) {
        Map<String, StockQuote> result = new HashMap<>();

        for (String ticker : tickers) {
            fetchQuote(ticker)
                    .ifPresent(quote -> result.put(ticker, quote));
        }

        return result;
    }

    @Override
    public String getProviderName() {
        return "YAHOO_FINANCE";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}