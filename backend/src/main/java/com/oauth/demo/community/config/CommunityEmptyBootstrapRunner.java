package com.oauth.demo.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds demo community content when the database has no posts (production-friendly).
 * Enable with community.bootstrap.on-empty=true
 */
@Component
@ConditionalOnProperty(name = "community.bootstrap.on-empty", havingValue = "true")
public class CommunityEmptyBootstrapRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CommunityEmptyBootstrapRunner.class);

    private final CommunityDemoSeeder seeder;

    public CommunityEmptyBootstrapRunner(CommunityDemoSeeder seeder) {
        this.seeder = seeder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking community feed bootstrap (on-empty)…");
        seeder.runSeed();
    }
}
