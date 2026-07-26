package org.workshop.marketcanvas;

import org.junit.jupiter.api.Test;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;
import org.workshop.marketcanvas.sharedkernel.domain.UserId;
import org.workshop.marketcanvas.watchlist.domain.Watchlist;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WatchlistTest {
    @Test
    void addAsset_throwsException_whenAddingEleventhAsset() {
        // 1. Arrange: Create the Watchlist with dummy IDs
        UserId dummyOwnerId = new UserId(UUID.randomUUID());
        Watchlist watchlist = Watchlist.create(dummyOwnerId, "Watch list 1");

        // Fill the watchlist to its maximum capacity (10 assets)
        for (int i = 0; i < 10; i++) {
            watchlist.addAsset(new AssetId(UUID.randomUUID()));
        }

        // 2. Act & Assert: The 11th attempt must throw an IllegalStateException
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> watchlist.addAsset(new AssetId(UUID.randomUUID())),
                "Expected addAsset to throw, but it didn't"
        );

        // 3. Verify the exact message to ensure we caught the right error
        assertEquals("Free-Tier Watchlists can hold a maximum of 10 Assets", exception.getMessage());
    }
}
