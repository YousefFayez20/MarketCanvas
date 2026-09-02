package org.workshop.marketcanvas.marketdata.application;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;
import org.workshop.marketcanvas.marketdata.infrastructure.MarketDataBroadcaster;
import org.workshop.marketcanvas.marketdata.infrastructure.adapters.FinnhubMarketDataProvider;
import org.workshop.marketcanvas.marketdata.infrastructure.adapters.YahooFinanceMarketDataProvider;
import org.workshop.marketcanvas.marketdata.infrastructure.persistence.AssetQuoteEntity;
import org.workshop.marketcanvas.marketdata.infrastructure.persistence.AssetQuoteRepository;
import org.workshop.marketcanvas.marketdata.infrastructure.persistence.cache.MultiTierMarketDataCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.workshop.marketcanvas.marketdata.infrastructure.persistence.AssetQuoteEntity;

@Service
@Slf4j
public class ResilientMarketDataService {
    private final FinnhubMarketDataProvider finnhubMarketDataProvider;
    private final YahooFinanceMarketDataProvider yahooFinanceMarketDataProvider;
    private final MultiTierMarketDataCache cache;
    private final AssetQuoteRepository assetQuoteRepository;
    private final ConcurrentHashMap<String, CompletableFuture<StockQuote>> inflightRequests = new ConcurrentHashMap<>();
    private final MarketDataBroadcaster marketDataBroadcaster;


    public ResilientMarketDataService(FinnhubMarketDataProvider finnhubMarketDataProvider, YahooFinanceMarketDataProvider yahooFinanceMarketDataProvider, MultiTierMarketDataCache cache, AssetQuoteRepository assetQuoteRepository, MarketDataBroadcaster marketDataBroadcaster) {
        this.finnhubMarketDataProvider = finnhubMarketDataProvider;
        this.yahooFinanceMarketDataProvider = yahooFinanceMarketDataProvider;
        this.cache = cache;
        this.assetQuoteRepository = assetQuoteRepository;
        this.marketDataBroadcaster = marketDataBroadcaster;
    }

    public StockQuote getQuote(String ticker){
        Optional<StockQuote> quote = cache.get(ticker);
        if(quote.isPresent()){
            return quote.get();
        }
        Optional<AssetQuoteEntity> assetQuoteEntity = assetQuoteRepository.findById(ticker);
        if(assetQuoteEntity.isPresent()){
            StockQuote stockQuote = assetQuoteEntity.get().toDomain();
            cache.put(ticker,stockQuote);
            return stockQuote;
        }
        CompletableFuture<StockQuote> future =
                inflightRequests.computeIfAbsent(ticker, key ->
                        CompletableFuture.supplyAsync(() -> {

                            // Fetch from external provider
                            StockQuote stockQuote = fetchQuote(key).get();

                            // Save to DB
                            assetQuoteRepository.save(
                                    AssetQuoteEntity.fromDomain(stockQuote)
                            );

                            // Save to cache
                            cache.put(key, stockQuote);

                            return stockQuote;
                        }).exceptionally(ex -> {
                            log.error("Failed to fetch quote for {}", key, ex);
                            throw new RuntimeException("Failed to fetch quote", ex);
                        })
                );

        // 4. Clean up regardless of success/failure
        future.whenComplete((result, error) ->
                inflightRequests.remove(ticker)
        );

        return future.join();
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
    public Map<String, StockQuote> refreshSnapshot(List<String> tickers) {
        Map<String, StockQuote> result = new HashMap<>();
        for (String ticker : tickers) {
            fetchQuote(ticker).ifPresent(quote -> {
                assetQuoteRepository.save(AssetQuoteEntity.fromDomain(quote));
                cache.put(ticker, quote);
                result.put(ticker, quote);
                marketDataBroadcaster.broadcast(quote);
            });
        }
        return result;
    }

}
