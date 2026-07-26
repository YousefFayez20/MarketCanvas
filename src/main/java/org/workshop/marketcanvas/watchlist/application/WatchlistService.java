package org.workshop.marketcanvas.watchlist.application;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.sharedkernel.domain.UserId;
import org.workshop.marketcanvas.watchlist.domain.Watchlist;
import org.workshop.marketcanvas.watchlist.infrastructure.WatchlistRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;


    @Transactional
    public UUID createWatchlist(UserId ownerId, String name){
        Watchlist watchlist = Watchlist.create(ownerId,name);
        Watchlist saved = watchlistRepository.save(watchlist);
// Note: calling repository.save() automatically extracts and publishes
        // the @DomainEvents queued inside the aggregate!
        return saved.getId();
    }
    @Transactional
    public void addAssetToWatchlist(UUID watchlistId, AssetId assetId){
        Watchlist watchlist = watchlistRepository.findById(watchlistId).orElseThrow(() -> new IllegalArgumentException("Watchlist not found"));
        watchlist.addAsset(assetId);
        watchlistRepository.save(watchlist);
    }
}
