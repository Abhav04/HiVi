package com.oauth.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupConfig {

    private static final Logger log = LoggerFactory.getLogger(StartupConfig.class);

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        log.info("Active profiles: {}", String.join(",", env.getActiveProfiles()));
        log.info("Server port: {}", env.getProperty("server.port"));
        log.info("DATABASE_URL set: {}", env.getProperty("DATABASE_URL") != null);
        log.info("DB_HOST set: {}", env.getProperty("DB_HOST") != null);
        log.info("DB_USERNAME set: {}", env.getProperty("DB_USERNAME") != null);
    }
}
