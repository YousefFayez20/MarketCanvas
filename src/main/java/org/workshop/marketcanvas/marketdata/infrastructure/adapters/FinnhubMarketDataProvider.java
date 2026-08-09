package org.workshop.marketcanvas.marketdata.infrastructure.adapters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.workshop.marketcanvas.marketdata.domain.MarketDataProvider;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class FinnhubMarketDataProvider implements MarketDataProvider {
    private final RestClient restClient;
    private final String apiKey;

    public FinnhubMarketDataProvider(RestClient.Builder restClientBuilder ,  @Value("${marketdata.finnhub.api-key:}")  String apiKey) {
        this.restClient =  restClientBuilder
                .baseUrl("https://finnhub.io/api/v1")
                .build();
        this.apiKey = apiKey;
    }

    @Override
    public Optional<StockQuote> fetchQuote(String ticker) {
        try{
            FinnhubQuoteResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/quote")
                            .queryParam("symbol",ticker)
                            .queryParam("token",apiKey)
                            .build()).retrieve()
                    .body(FinnhubQuoteResponse.class);
            if (response == null
                    || response.c() == null
                    || response.c().compareTo(BigDecimal.ZERO) <= 0
                    || response.t() == null) {

                return Optional.empty();
            }
            StockQuote quote = new StockQuote(
                    ticker,
                    response.c(),
                    response.d(),
                    response.dp(),
                    response.h(),
                    response.l(),
                    response.o(),
                    response.pc(),
                    null,
                    Instant.ofEpochSecond(response.t()),
                    "FINNHUB"
            );
            return Optional.of(quote);
        } catch (Exception e) {
            log.error("Failed to fetch quote for ticker {}", ticker, e);            return Optional.empty();
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
        return "FINNHUB";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null
                && !apiKey.isBlank() && !apiKey.equalsIgnoreCase("demo");
    }
    private record FinnhubQuoteResponse(
            BigDecimal c,
            BigDecimal d,
            BigDecimal dp,
            BigDecimal h,
            BigDecimal l,
            BigDecimal o,
            BigDecimal pc,
            Long t
    ) {
    }
}
