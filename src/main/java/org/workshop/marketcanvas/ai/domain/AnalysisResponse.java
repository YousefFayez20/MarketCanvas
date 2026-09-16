package org.workshop.marketcanvas.ai.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;
public record AnalysisResponse(
        @JsonPropertyDescription(
                "The primary stock ticker symbol being analyzed, or 'PORTFOLIO' when the analysis covers multiple securities."
        )
        String ticker,

        @JsonPropertyDescription(
                "A concise 1-2 sentence executive summary of the analysis based only on the market data provided in the context. Do not introduce information that is not present in the context."
        )
        String summary,

        @JsonPropertyDescription(
                "Overall market sentiment based only on the provided data. Use BULLISH when the available metrics show predominantly positive momentum or price action, BEARISH when they show predominantly negative momentum or price action, and NEUTRAL when signals are mixed or insufficient to support a directional conclusion."
        )
        MarketSentiment sentiment,

        @JsonPropertyDescription(
                "The current risk level based on the volatility, trading volume, and price action shown in the provided data. Use LOW for relatively stable conditions, MEDIUM for moderate uncertainty or volatility, and HIGH for elevated volatility, unusual volume, or significant adverse price action."
        )
        RiskLevel riskLevel,

        @JsonPropertyDescription(
                "Provide 3 to 5 specific observations that directly support the analysis. Every observation must cite at least one actual metric value from the provided context, such as price, percentage change, volume, volatility, or another available indicator. Do not invent or estimate metric values."
        )
        List<String> keyPoints,

        @JsonPropertyDescription(
                "List the specific market metrics from the provided context that were used to form the analysis, including their actual values when available. Examples include 'Price: $232.45', 'Daily Change: +2.4%', or 'Volume: 12.4M'. Do not include metrics that were not provided in the context."
        )
        List<String> metricsObserved,

        @JsonPropertyDescription(
                "A standard risk disclaimer stating that the analysis is provided for informational purposes only and does not constitute financial advice, an investment recommendation, or a guarantee of future performance."
        )
        String disclaimer

) {
}