package org.workshop.marketcanvas.marketdata.infrastructure;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.workshop.marketcanvas.marketdata.application.AssetInfo;
import org.workshop.marketcanvas.marketdata.application.AssetRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetRegistry assetRegistry;

    @GetMapping
    public ResponseEntity<List<AssetInfo>> getAllAssets(){
        return ResponseEntity.ok(assetRegistry.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetInfo> getAsset(@PathVariable UUID id){
        Optional<AssetInfo> asset = assetRegistry.findById(id);
       return asset.map(ResponseEntity::ok)
               .orElseGet(()->ResponseEntity.notFound().build());

    }
    @GetMapping("/search")
    public ResponseEntity<List<AssetInfo>> searchAssets(
            @RequestParam("q") String q) {
        return ResponseEntity.ok(assetRegistry.search(q));
    }
}
