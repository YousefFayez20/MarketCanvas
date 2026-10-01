package org.workshop.marketcanvas.ai.application;


import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.workshop.marketcanvas.ai.domain.WatchlistAnalysis;
import org.workshop.marketcanvas.marketdata.application.AssetInfo;
import org.workshop.marketcanvas.marketdata.application.AssetRegistry;
import org.workshop.marketcanvas.marketdata.application.ResilientMarketDataService;
import org.workshop.marketcanvas.marketdata.domain.StockQuote;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.watchlist.domain.Watchlist;
import org.workshop.marketcanvas.watchlist.infrastructure.WatchlistRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WatchlistAnalysisService {
    private final ChatClient chatClient;
    private final WatchlistRepository watchlistRepository;
    private final AssetRegistry assetRegistry;
    private final ResilientMarketDataService marketDataService;
    private final Resource promptTemplate;
    public WatchlistAnalysisService(
            ChatClient.Builder chatClientBuilder,
            WatchlistRepository watchlistRepository,
            AssetRegistry assetRegistry,
            ResilientMarketDataService marketDataService,
            @Value("classpath:prompts/watchlist-analyst.txt") Resource promptTemplate) {
        this.chatClient = chatClientBuilder.build();
        this.watchlistRepository = watchlistRepository;
        this.assetRegistry = assetRegistry;
        this.marketDataService = marketDataService;
        this.promptTemplate = promptTemplate;
    }
    public WatchlistAnalysis analyzeWatchlist(UUID watchlistId){
        //fetch watchlist
        Watchlist watchlist = watchlistRepository.findById(watchlistId).orElseThrow(
                () -> new EntityNotFoundException("Watchlist not found:" + watchlistId)
        );
        //get assets ids
        Set<AssetId> assetIds = watchlist.getAssets();
        if(assetIds.isEmpty()){
            return new WatchlistAnalysis(
                    0,
                    org.workshop.marketcanvas.ai.domain.DiversificationRating.CONCENTRATED,
                    Collections.emptyMap(),
                    List.of("Portfolio has no assets"),
                    List.of("Add at least 3-5 assets across different sectors to evaluate diversification"),
                    "Your watchlist is currently empty. Add stocks to receive a personalized portfolio health audit.",
                    "This assessment is for educational purposes and does not constitute financial advice."
            );
        }
        //get assets from registry by assets ID
        List<AssetInfo> assetInfos = assetIds.stream().map(id -> assetRegistry.findById(id.value()))
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
        //compute sector breakdown
        Map<String,Double> sectorBreakdown = computeSectorPercentages(assetInfos);
        //build context containing quotes & sectors and watchlist name
        String context = buildPortfolioContext(watchlist.getName(),assetInfos,sectorBreakdown);
        //load system prompt
        String systemPrompt = loadPromptTemplate().replace("{portfolio_context}", context).replace("{context}", context);
        //query gemini with structured output "Analyze the health, sector diversification, and risk posture of my watchlist."
        return chatClient.prompt().system(systemPrompt).user("Analyze the health, sector diversification, and risk posture of my watchlist.")
                .call().entity(WatchlistAnalysis.class);


    }
    private String buildPortfolioContext(String watchlistName, List<AssetInfo>assets, Map<String, Double> sectorBreakdown){
        StringBuilder context = new StringBuilder();
        context.append("--- WATCHLIST CONTEXT ---\n\n");
        context.append("Watchlist Name: ").append(watchlistName).append("\n\n");
        for(AssetInfo asset : assets){
            try {
                StockQuote quote = marketDataService.getQuote(asset.ticker());
                if (quote != null) {
                    context.append("Ticker: ")
                            .append(quote.ticker())
                            .append(" (")
                            .append(asset.name())
                            .append(" | Sector: ")
                            .append(asset.sector() != null ? asset.sector() : "N/A")
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
                            .append(quote.volume() != null ? quote.volume() : "N/A")
                            .append("\n");
                    context.append("Last Updated: ")
                            .append(quote.lastUpdated())
                            .append("\n");
                    context.append("Provider Source: ")
                            .append(quote.providerSource())
                            .append("\n\n");
                }
            } catch (Exception e) {
                log.warn("Failed to fetch live quote for {}: {}", asset.ticker(), e.getMessage());
                context.append("Ticker: ").append(asset.ticker())
                        .append(" (").append(asset.name()).append(" | Sector: ").append(asset.sector())
                        .append(") - Price data temporarily unavailable\n\n");
            }
        }
        context.append("Sector Diversity: ").append("\n\n");
        for(Map.Entry<String, Double> entry: sectorBreakdown.entrySet() ){
            context.append("Sector: ").append(entry.getKey()).append(" -> ")
                    .append(entry.getValue()).append("%\n");
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
    private Map<String, Double> computeSectorPercentages(List<AssetInfo> assets) {
        if (assets == null || assets.isEmpty()) {
            return Collections.emptyMap();
        }

        double totalAssets = assets.size();

        return assets.stream()
                .collect(Collectors.groupingBy(
                        asset -> Optional.ofNullable(asset.sector()) // Use asset.getSector() if AssetInfo is a standard class
                                .filter(s -> !s.isBlank())
                                .orElse("Unknown"),
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                count -> roundToTwoDecimals((count / totalAssets) * 100.0)
                        )
                ));
    }

    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
    private String loadPromptTemplate(){
        try(var inputStream = promptTemplate.getInputStream()){
            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e){
            throw new IllegalStateException ("Failed to load AI market analyst prompt template",
                    e
            );
        }
    }

}
