package org.workshop.marketcanvas.marketdata.infrastructure;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.workshop.marketcanvas.marketdata.application.AssetInfo;
import org.workshop.marketcanvas.marketdata.application.AssetRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.util.concurrent.CompletableFuture;
import org.workshop.marketcanvas.marketdata.application.ResilientMarketDataService;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AssetController {
    private final AssetRegistry assetRegistry;
    private final MarketDataBroadcaster marketDataBroadcaster;
    private final ResilientMarketDataService resilientService;

    @GetMapping
    public ResponseEntity<List<AssetInfo>> getAllAssets(){
        return ResponseEntity.ok(assetRegistry.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetInfo> getAsset(@PathVariable UUID id){
        Optional<AssetInfo> asset = assetRegistry.findById(id);
       return asset.map(ResponseEntity::ok)
               .orElseGet(()->ResponseEntity.notFound().build());

    }
    @GetMapping("/search")
    public ResponseEntity<List<AssetInfo>> searchAssets(
            @RequestParam("q") String q) {
        return ResponseEntity.ok(assetRegistry.search(q));
    }
    @GetMapping(value = "/stream",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamPriceUpdates(){
        SseEmitter emitter = new SseEmitter(0L);
        marketDataBroadcaster.addEmitter(emitter);

        // Immediately send the latest known quotes to the new subscriber
        // so they don't have to wait for the scheduler's next rotation.
        CompletableFuture.runAsync(() -> {
            for (AssetInfo asset : assetRegistry.findAll()) {
                try {
                    org.workshop.marketcanvas.marketdata.domain.StockQuote quote = resilientService.getQuote(asset.ticker());
                    emitter.send(SseEmitter.event().name("price-update").data(quote));
                } catch (Exception ex) {
                    // Ignore errors for individual quotes during initial sync
                }
            }
        });

        return emitter;
    }
}
