package org.workshop.marketcanvas.ai.infrastructure;

import java.util.List;
import jakarta.validation.constraints.NotBlank;
public record AnalysisRequest(
        @NotBlank(message = "Question must not be empty") String question,
List<String> tickers
) {

}
