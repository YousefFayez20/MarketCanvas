package org.workshop.marketcanvas.marketdata.application;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;
import org.workshop.marketcanvas.marketdata.infrastructure.adapters.FinnhubMarketDataProvider;
import org.workshop.marketcanvas.marketdata.infrastructure.adapters.YahooFinanceMarketDataProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ResilientMarketDataService {
    private final FinnhubMarketDataProvider finnhubMarketDataProvider;
    private final YahooFinanceMarketDataProvider yahooFinanceMarketDataProvider;


    public ResilientMarketDataService(FinnhubMarketDataProvider finnhubMarketDataProvider, YahooFinanceMarketDataProvider yahooFinanceMarketDataProvider) {
        this.finnhubMarketDataProvider = finnhubMarketDataProvider;
        this.yahooFinanceMarketDataProvider = yahooFinanceMarketDataProvider;
    }
    @CircuitBreaker(
            name = "finnhub",
            fallbackMethod = "fetchQuoteFallback"
    )
    @RateLimiter(
            name = "finnhub",
            fallbackMethod = "fetchQuoteFallback"
    )
    public Optional<StockQuote> fetchQuote(String ticker){
        if (!finnhubMarketDataProvider.isAvailable()){
            log.info("Finnhub not available. Using Yahoo Finance for {}",
                    ticker);
            return yahooFinanceMarketDataProvider.fetchQuote(ticker);
        }
        return finnhubMarketDataProvider.fetchQuote(ticker);
    }

    public Optional<StockQuote> fetchQuoteFallback(
            String ticker,
            Throwable ex
    ) {
        log.warn(
                "Finnhub failed for ticker {} [{}]. " +
                        "Failing over to Yahoo Finance.",
                ticker,
                ex.getClass().getSimpleName()
        );

        return yahooFinanceMarketDataProvider.fetchQuote(ticker);
    }
    public Map<String, StockQuote> fetchBatchQuotes(
            List<String> tickers
    ) {
        Map<String, StockQuote> result = new HashMap<>();

        for (String ticker : tickers) {
            fetchQuote(ticker)
                    .ifPresent(quote -> result.put(ticker, quote));
        }

        return result;
    }

}
