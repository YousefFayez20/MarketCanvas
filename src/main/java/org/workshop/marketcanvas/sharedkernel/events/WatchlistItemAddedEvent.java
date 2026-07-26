package org.workshop.marketcanvas.sharedkernel.events;

import org.workshop.marketcanvas.sharedkernel.domain.AssetId;

import java.time.Instant;
import java.util.UUID;

public record WatchlistItemAddedEvent(UUID watchlistId,
                                      UUID assetId,
                                      Instant timestamp
) {}
