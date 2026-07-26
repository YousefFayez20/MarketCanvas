package org.workshop.marketcanvas.watchlist.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.workshop.marketcanvas.watchlist.domain.Watchlist;

import java.util.UUID;

public interface WatchlistRepository
extends JpaRepository<Watchlist, UUID> {
}
