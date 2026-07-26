package org.workshop.marketcanvas.watchlist.domain;

import jakarta.persistence.*;
import lombok.*;
import org.jmolecules.event.annotation.DomainEvent;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.DomainEvents;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.sharedkernel.domain.UserId;
import org.workshop.marketcanvas.sharedkernel.events.WatchlistItemAddedEvent;

import java.time.Instant;
import java.util.*;

@Entity
@AllArgsConstructor
@Getter
public class Watchlist {
    @Id
    private final UUID id;
    @Column(nullable = false)
    private final UserId ownerId;
    @Column(nullable = false, length = 50)
    private String name;
    @ElementCollection
    @CollectionTable(
            name = "watchlist_assets",
            joinColumns = @JoinColumn(name = "watchlist_id")
    )
    @Column(name = "asset_id") // Name of the column in the collection table
    private final Set<AssetId> assets;
    protected Watchlist() {
        this.id = null;
        this.ownerId = null;
        this.assets = new HashSet<>();
    }
    @Transient
    private List<Object> domainEvents = new ArrayList<>();
    private Watchlist(UUID id, UserId ownerId, String name) {
        this.id = id;
        this.ownerId = ownerId;
        this.assets = new HashSet<>();
        rename(name); // Reuse the validation logic!
    }
    // Factory Method
    public static Watchlist create(UserId ownerId, String name) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        return new Watchlist(UUID.randomUUID(), ownerId, name);
    }
    public void rename(String newName) {
        // TODO: Implement FR2 validation
        if(newName == null || newName.trim().isEmpty()){
            throw new IllegalArgumentException("name can't be null");
        } else if (newName.length() > 50) {
            throw new IllegalArgumentException("name must be less than 50 characters");
        }
         this.name = newName;
    }


    public void addAsset(AssetId assetId) {
        if(assetId == null){
            throw new IllegalArgumentException("AssetId cannot be null");
        }
        if(assets.contains(assetId)){
           return;
        }
        // TODO: Implement FR3 (Max 10 assets) and FR4 (Uniqueness/Set behavior)
        if(this.assets.size() >= 10){
            throw new IllegalStateException("Free-Tier Watchlists can hold a maximum of 10 Assets");
        }
        this.assets.add(assetId);
        this.domainEvents.add(new WatchlistItemAddedEvent(this.id, assetId.value(), Instant.now()));
    }

    public void removeAsset(AssetId assetId) {
        if (assetId != null) {
            this.assets.remove(assetId);
        }
    }
    @DomainEvents
    Collection<Object> domainEvents() {
        return domainEvents;
    }
    @AfterDomainEventPublication
    void clearEvents() {
        domainEvents.clear();
    }
}
