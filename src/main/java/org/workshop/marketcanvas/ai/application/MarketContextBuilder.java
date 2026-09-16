package org.workshop.marketcanvas.ai.application;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.workshop.marketcanvas.marketdata.application.AssetInfo;
import org.workshop.marketcanvas.marketdata.application.AssetRegistry;
import org.workshop.marketcanvas.marketdata.application.ResilientMarketDataService;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MarketContextBuilder {
    private final ResilientMarketDataService marketDataService;
    private final AssetRegistry assetRegistry;
    public String buildContext(List<String> tickers){
        StringBuilder context = new StringBuilder();
        context.append("--- MARKET DATA ---\n\n");
        List<StockQuote> stockQuotes= new ArrayList<>();
        List<AssetInfo> assetInfos = assetRegistry.findAll();
        for(String ticker : tickers) {
            try {
                AssetInfo asset = assetInfos.stream()
                        .filter(assetInfo1 -> assetInfo1.ticker().equalsIgnoreCase(ticker))
                        .findFirst()
                        .orElse(null);
                StockQuote quote = marketDataService.getQuote(ticker);

                stockQuotes.add(quote);
                context.append("Ticker: ")
                        .append(quote.ticker())
                        .append(" (")
                        .append(asset != null ? asset.name() : quote.ticker())
                        .append(" | Sector: ")
                        .append(asset != null ? asset.sector() : "N/A")
                        .append(")\n\n");

                context.append("Price: $")
                        .append(format(quote.price()))
                        .append("\n");
                context.append("24h Change: ")
                        .append(formatSigned(quote.change24h()))
                        .append(" (")
                        .append(formatSigned(quote.changePercent24h()))
                        .append("%)\n\n");
                context.append("24h High: $")
                        .append(format(quote.high24h()))
                        .append("\n");

                context.append("24h Low: $")
                        .append(format(quote.low24h()))
                        .append("\n");

                context.append("Open Price: $")
                        .append(format(quote.openPrice()))
                        .append("\n");

                context.append("Previous Close: $")
                        .append(format(quote.previousClose()))
                        .append("\n");
                context.append("Volume: ")
                        .append(quote.volume())
                        .append("\n");

                context.append("Last Updated: ")
                        .append(quote.lastUpdated())
                        .append("\n");
                context.append("Provider Source: ")
                        .append(quote.providerSource())
                        .append("\n\n");
            } catch (Exception ex) {
                context.append("Ticker: ") .append(ticker) .append(" — No current data available\n\n");
            }
        }
        return context.toString();

    }

    private String format(BigDecimal value){
        if (value == null) return "N/A";
        return value.setScale(2,RoundingMode.HALF_UP).toPlainString();
    }
    private String formatSigned(BigDecimal value) {
        if (value == null) return "N/A";
        BigDecimal rounded = value.setScale(2, RoundingMode.HALF_UP);
        if (rounded.signum() > 0) {
            return "+" + rounded.toPlainString();
        }
        return rounded.toPlainString();
    }
}
