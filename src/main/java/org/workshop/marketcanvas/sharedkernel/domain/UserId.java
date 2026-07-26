package org.workshop.marketcanvas.sharedkernel.domain;

import java.util.UUID;

public record UserId(UUID value) {
    public UserId{
        if(value == null){
            throw new IllegalArgumentException("UserId cannot be null");
        }
    }
    public static UserId generate(){
        return new UserId(UUID.randomUUID());
    }
}
