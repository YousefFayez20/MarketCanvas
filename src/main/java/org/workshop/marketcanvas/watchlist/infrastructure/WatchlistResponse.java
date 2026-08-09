package org.workshop.marketcanvas.watchlist.infrastructure;

import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.watchlist.domain.Watchlist;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record WatchlistResponse(
        UUID id,
        UUID ownerId,
        String name,
        Set<UUID> assetIds,
        int assetCount
) {

    public static WatchlistResponse from(Watchlist watchlist){
        return new WatchlistResponse(
          watchlist.getId(),
               watchlist.getOwnerId().value(),
          watchlist.getName(),
          watchlist.getAssets().stream()
                  .map(AssetId::value)
                  .collect(Collectors.toSet()),
                watchlist.getAssets().size()

        );
    }

}