package org.workshop.marketcanvas.marketdata.domain;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MarketDataProvider {
    Optional<StockQuote> fetchQuote(String ticker);
    Map<String, StockQuote> fetchBatchQuotes(List<String> tickers);
    String getProviderName();
    boolean isAvailable();
}
