package org.workshop.marketcanvas.watchlist.infrastructure;

import java.util.UUID;

public record CreateWatchlistRequest(UUID ownerId, String name) {
}
