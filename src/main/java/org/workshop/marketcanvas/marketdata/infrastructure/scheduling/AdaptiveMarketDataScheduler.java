package org.workshop.marketcanvas.marketdata.infrastructure.scheduling;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.workshop.marketcanvas.marketdata.application.AssetInfo;
import org.workshop.marketcanvas.marketdata.application.AssetRegistry;
import org.workshop.marketcanvas.marketdata.application.ResilientMarketDataService;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdaptiveMarketDataScheduler {
    final private ResilientMarketDataService resilientService;
    final private AssetRegistry assetRegistry;
    private final Queue<String> tickerRotationQueue = new ConcurrentLinkedQueue<>();
    @PostConstruct
    public void initializeQueue(){
       List<AssetInfo> assetInfos = assetRegistry.findAll();
       for (AssetInfo assetInfo: assetInfos){
           tickerRotationQueue.add(assetInfo.ticker());
       }
    }
    private boolean isMarketHours() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        DayOfWeek day = now.getDayOfWeek();
        LocalTime time = now.toLocalTime();
        boolean isWeekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        boolean isDuringHours = !time.isBefore(LocalTime.of(9, 30)) && !time.isAfter(LocalTime.of(16, 0));
        return isWeekday && isDuringHours;
    }

    @Scheduled(fixedDelay = 12_000)
    public void adaptiveRefreshCycle(){
        List<String> batch = new ArrayList<>();
        int count = 0;
        while (count < 10 && !tickerRotationQueue.isEmpty()){
            batch.add(tickerRotationQueue.poll());
            count++;
        }
        resilientService.refreshSnapshot(batch);
        for(String ticker: batch){
            tickerRotationQueue.add(ticker);
        }
    }

}
