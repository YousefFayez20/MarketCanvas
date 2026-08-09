package org.workshop.marketcanvas.watchlist.application;


import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.sharedkernel.domain.UserId;
import org.workshop.marketcanvas.sharedkernel.infrastructure.AssetIdConverter;
import org.workshop.marketcanvas.watchlist.domain.Watchlist;
import org.workshop.marketcanvas.watchlist.infrastructure.WatchlistRepository;
import java.util.List;

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
        Watchlist watchlist = requireWatchlist(watchlistId);
        watchlist.addAsset(assetId);
        watchlistRepository.save(watchlist);
    }
    @Transactional(readOnly = true)
    public List<Watchlist> getWatchlistsByOwner(UserId ownerId){
        return watchlistRepository.findByOwnerId(ownerId);
    }
    @Transactional(readOnly = true)
    public Watchlist getWatchlist(UUID id){
        return requireWatchlist(id);
    }
    @Transactional
    public void deleteWatchlist(UUID id){
        Watchlist watchlist = requireWatchlist(id);
        watchlistRepository.delete(watchlist);
    }
    @Transactional
    public void removeAssetFromWatchlist(UUID watchlistId, UUID assetId){
        Watchlist watchlist = requireWatchlist(watchlistId);
        watchlist.removeAsset(new AssetId(assetId));
    }
    private Watchlist requireWatchlist(UUID id) {
        return watchlistRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Watchlist not found"));
    }
}
