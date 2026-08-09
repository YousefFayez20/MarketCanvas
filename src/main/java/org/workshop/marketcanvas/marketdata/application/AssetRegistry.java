package org.workshop.marketcanvas.marketdata.application;


import tools.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetRegistry {
    private final ObjectMapper objectMapper;
    private final List<AssetInfo> assets = new ArrayList<>();

    @PostConstruct
    void loadAssets() {
        InputStream inputStream = getClass().getResourceAsStream("/data/assets.json");
        if (inputStream == null) {
            log.warn("Assets file not found at /data/assets.json");
            return;
        }
        try {
            List<AssetInfo> loadedAssets = objectMapper.readValue(inputStream, new TypeReference<List<AssetInfo>>() {});
            assets.clear();
            assets.addAll(loadedAssets);
        } catch (Exception e) {
            log.error("Failed to load market data JSON file", e);
            throw new IllegalStateException("Could not initialize AssetRegistry", e);
        }
    }
    public Optional<AssetInfo> findById(UUID id){
        return assets.stream().filter(assetInfo -> assetInfo.id().equals(id)).findFirst();
    }
    public List<AssetInfo> findAll(){
       return List.copyOf(assets);
    }
    public List<AssetInfo> search(String query){
        if(query==null || query.isEmpty()){
            return findAll();
        }
        String normalized = query.toLowerCase();

        return assets.stream()
                .filter(assetInfo -> assetInfo.ticker().toLowerCase().contains(normalized)
                || assetInfo.name().toLowerCase().contains(normalized) ).toList();
    }
}
