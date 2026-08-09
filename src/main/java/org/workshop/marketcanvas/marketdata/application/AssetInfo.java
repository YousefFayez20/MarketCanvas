package org.workshop.marketcanvas.marketdata.application;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public record AssetInfo(UUID id, String ticker, String name, String sector) {
    public AssetInfo {
        if (id == null && ticker != null) {
            id = UUID.nameUUIDFromBytes(ticker.getBytes(StandardCharsets.UTF_8));
        }
    }
}
