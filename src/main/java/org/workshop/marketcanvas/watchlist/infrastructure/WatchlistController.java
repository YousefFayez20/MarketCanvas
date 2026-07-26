package org.workshop.marketcanvas.watchlist.infrastructure;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.sharedkernel.domain.UserId;
import org.workshop.marketcanvas.sharedkernel.infrastructure.UserIdConverter;
import org.workshop.marketcanvas.watchlist.application.WatchlistService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/watchlists")
public class WatchlistController {
    private final WatchlistService watchlistService;

    @PostMapping
    public ResponseEntity<UUID> createWatchlist(@RequestBody CreateWatchlistRequest watchlistRequest){
        UserId owner = new UserId(watchlistRequest.ownerId());
        UUID id = watchlistService.createWatchlist(owner,watchlistRequest.name());
        return ResponseEntity.ok(id);
    }

    @PostMapping("/{id}/assets")
    public ResponseEntity<Void> addAsset(@PathVariable UUID id, @RequestBody AddAssetRequest request){
        AssetId asset = new AssetId(request.assetId());
        watchlistService.addAssetToWatchlist(id,asset);
        return ResponseEntity.ok().build();
    }
}
