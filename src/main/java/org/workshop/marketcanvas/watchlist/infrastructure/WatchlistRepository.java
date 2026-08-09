package org.workshop.marketcanvas.watchlist.infrastructure;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.workshop.marketcanvas.sharedkernel.domain.UserId;
import org.workshop.marketcanvas.watchlist.domain.Watchlist;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WatchlistRepository
extends JpaRepository<Watchlist, UUID> {
    
    @EntityGraph(attributePaths = {"assets"})
    List<Watchlist> findByOwnerId(UserId ownerId);

    @EntityGraph(attributePaths = {"assets"})
    Optional<Watchlist> findById(UUID id);
}
