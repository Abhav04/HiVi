package com.oauth.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class OAuthStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(OAuthStartupLogger.class);

    private final Environment env;
    private final OAuthRegistrationDiagnostics diagnostics;

    public OAuthStartupLogger(Environment env, OAuthRegistrationDiagnostics diagnostics) {
        this.env = env;
        this.diagnostics = diagnostics;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logRegistrations() {
        for (String provider : new String[] {"google", "github"}) {
            Map<String, Object> snap = diagnostics.registrationSnapshot(provider);
            if (Boolean.TRUE.equals(snap.get("loaded"))) {
                log.info(
                        "OAuth registration [{}]: redirectUri={} clientIdPrefix={}",
                        provider,
                        snap.get("redirectUri"),
                        snap.get("clientIdPrefix"));
            } else {
                log.warn("OAuth registration [{}] not loaded: {}", provider, snap.get("error"));
            }
        }

        if (Arrays.asList(env.getActiveProfiles()).contains("local")) {
            log.info(
                    "Local Google OAuth: add redirect URI on the SAME client as GOOGLE_CLIENT_ID → {}",
                    diagnostics.registrationSnapshot("google").get("redirectUri"));
            log.info(
                    "Google Console must include client id prefix: {}",
                    diagnostics.registrationSnapshot("google").get("clientIdPrefix"));
        }
    }
}
