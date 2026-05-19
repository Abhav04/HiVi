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
    public void onReady(Environment env) {
        String[] profiles = env.getActiveProfiles();
        String dbUrl = env.getProperty("spring.datasource.url", "not set");
        String maskedUrl = dbUrl.replaceAll("://([^:]+):([^@]+)@", "://***:***@");
        log.info("Started with profiles: {}", profiles.length > 0 ? String.join(",", profiles) : "default");
        log.info("Datasource URL: {}", maskedUrl);
        log.info("Server port: {}", env.getProperty("server.port"));
        log.info("DATABASE_URL present: {}", env.getProperty("DATABASE_URL") != null);
    }
}
