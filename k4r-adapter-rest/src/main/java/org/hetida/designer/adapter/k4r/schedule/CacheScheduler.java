package org.hetida.designer.adapter.k4r.schedule;

import lombok.extern.log4j.Log4j2;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class CacheScheduler {

    private final CacheManager cacheManager;

    public CacheScheduler(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictAllCaches() {

        cacheManager.getCacheNames().stream().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    // Evict cache every 5 Min.
    @Scheduled(fixedDelayString = "${k4r.cron.scheduler.interval}")
    public void evictAllcachesAtIntervals() {

        log.info("Clearing Cache...");
        evictAllCaches();
    }
}
