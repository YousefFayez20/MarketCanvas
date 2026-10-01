package org.workshop.marketcanvas.ai.infrastructure;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.marketcanvas.ai.application.WatchlistAnalysisService;
import org.workshop.marketcanvas.ai.domain.WatchlistAnalysis;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai/watchlists")
@CrossOrigin(origins = "*")
@Slf4j
public class WatchlistAiController {
    private final WatchlistAnalysisService analysisService;
    public WatchlistAiController(WatchlistAnalysisService analysisService) {
        this.analysisService = analysisService;
    }
    @PostMapping("/{watchlistId}/analyze")
    public ResponseEntity<WatchlistAnalysis> analyzeWatchlist(@PathVariable UUID watchlistId) {
        try {
            WatchlistAnalysis result = analysisService.analyzeWatchlist(watchlistId);
            return ResponseEntity.ok(result);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Failed to analyze watchlist {}: {}", watchlistId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }
}