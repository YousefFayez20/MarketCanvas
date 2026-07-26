package org.workshop.marketcanvas.sharedkernel.domain;

import java.util.UUID;

public record AssetId(UUID value) {
    public AssetId{
        if(value == null){
            throw new IllegalArgumentException("Asset ID can't be null");
        }
    }
    public static AssetId generate(){
        return new AssetId(UUID.randomUUID());
    }
}
