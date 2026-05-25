package com.oauth.demo.opportunities.scheduler;

import com.oauth.demo.opportunities.config.OpportunityProperties;
import com.oauth.demo.opportunities.service.OpportunityIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "opportunities.enabled", havingValue = "true", matchIfMissing = true)
public class OpportunityRefreshScheduler {

    private static final Logger log = LoggerFactory.getLogger(OpportunityRefreshScheduler.class);

    private final OpportunityIngestionService ingestionService;
    private final OpportunityProperties properties;

    public OpportunityRefreshScheduler(
            OpportunityIngestionService ingestionService,
            OpportunityProperties properties) {
        this.ingestionService = ingestionService;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${opportunities.refresh-interval-ms:900000}")
    public void refresh() {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            ingestionService.refreshExternalSources();
        } catch (Exception ex) {
            log.error("Scheduled opportunity refresh failed: {}", ex.getMessage());
        }
    }
}
