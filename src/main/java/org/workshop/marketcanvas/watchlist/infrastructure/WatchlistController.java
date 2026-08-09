package org.workshop.marketcanvas.watchlist.infrastructure;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.sharedkernel.domain.UserId;
import org.workshop.marketcanvas.watchlist.application.WatchlistService;

import java.util.List;
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
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PostMapping("/{id}/assets")
    public ResponseEntity<Void> addAsset(@PathVariable UUID id, @RequestBody AddAssetRequest request){
        AssetId asset = new AssetId(request.assetId());
        watchlistService.addAssetToWatchlist(id,asset);
        return ResponseEntity.ok().build();
    }
    @GetMapping
    public ResponseEntity<List<WatchlistResponse>> getWatchlistsByOwner(@RequestParam UUID ownerId){
        List<WatchlistResponse> responses = watchlistService.getWatchlistsByOwner(new UserId(ownerId)).stream()
                .map(WatchlistResponse::from).toList();
        return ResponseEntity.ok(responses);
    }
    @GetMapping("/{id}")
    public ResponseEntity<WatchlistResponse> getWatchlist(@PathVariable UUID id){

        WatchlistResponse watchlistResponse = WatchlistResponse.from(watchlistService.getWatchlist(id));
        return ResponseEntity.ok(watchlistResponse);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWatchlist(@PathVariable UUID id){

        watchlistService.deleteWatchlist(id);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/{id}/assets/{assetId}")
    public ResponseEntity<Void> removeAssetFromWatchlist(@PathVariable UUID id, @PathVariable UUID assetId){

        watchlistService.removeAssetFromWatchlist(id,assetId);
        return ResponseEntity.noContent().build();
    }


}
