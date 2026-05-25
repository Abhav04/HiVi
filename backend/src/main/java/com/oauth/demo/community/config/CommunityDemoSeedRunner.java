package com.oauth.demo.community.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "community.demo.seed", havingValue = "true")
public class CommunityDemoSeedRunner implements CommandLineRunner {

    private final CommunityDemoSeeder seeder;

    public CommunityDemoSeedRunner(CommunityDemoSeeder seeder) {
        this.seeder = seeder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seeder.runSeed();
    }
}
