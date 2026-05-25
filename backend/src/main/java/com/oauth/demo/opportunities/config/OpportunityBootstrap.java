package com.oauth.demo.opportunities.config;

import com.oauth.demo.opportunities.service.OpportunityIngestionService;
import com.oauth.demo.opportunities.service.OpportunityService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class OpportunityBootstrap implements ApplicationRunner {

    private final OpportunityService opportunityService;
    private final OpportunityIngestionService ingestionService;

    public OpportunityBootstrap(
            OpportunityService opportunityService,
            OpportunityIngestionService ingestionService) {
        this.opportunityService = opportunityService;
        this.ingestionService = ingestionService;
    }

    @Override
    public void run(ApplicationArguments args) {
        opportunityService.ensureSeeded();
        ingestionService.refreshExternalSources();
    }
}
