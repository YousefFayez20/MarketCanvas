package org.workshop.marketcanvas.ai.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;
import java.util.Map;

public record WatchlistAnalysis(
        @JsonPropertyDescription("A score from 0 to 100 reflecting the overall health, balance, and risk posture of the portfolio")
        int healthScore,
        @JsonPropertyDescription("Overall diversification level of the portfolio based on asset count and sector distribution")
        DiversificationRating diversificationRating,
        @JsonPropertyDescription("Percentage allocation per sector, formatted as a map where key is sector name and value is the percentage from 0.0 to 100.0")
        Map<String, Double> sectorBreakdown,
        @JsonPropertyDescription("List of the 2-4 primary risk factors identified in this portfolio (e.g. tech concentration, high volatility, macro sensitivity)")
        List<String> topRisks,
        @JsonPropertyDescription("List of 2-4 concrete, actionable diversification or rebalancing suggestions (e.g. consider defensive sectors, hedge volatility)")
        List<String> recommendations,
        @JsonPropertyDescription("A 2-3 sentence executive summary explaining the rating and the portfolio's general outlook")
        String executiveSummary,
        @JsonPropertyDescription("Standard financial disclaimer reminding the user that this analysis is educational and not financial advice")
        String disclaimer
) {
}
