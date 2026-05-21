package com.oauth.demo.reddit.scheduler;

import com.oauth.demo.reddit.service.RedditTrendingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RedditRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(RedditRefreshScheduler.class);

    private final RedditTrendingService trendingService;

    public RedditRefreshScheduler(RedditTrendingService trendingService) {
        this.trendingService = trendingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmCacheOnStartup() {
        log.info("Warming Reddit trending cache on application startup");
        trendingService.refreshCache();
    }

    @Scheduled(fixedDelayString = "${reddit.refresh-interval-ms:300000}")
    public void scheduledRefresh() {
        log.debug("Scheduled Reddit trending refresh");
        trendingService.refreshCache();
    }
}
