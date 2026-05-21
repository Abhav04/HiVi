package com.oauth.demo.config;

import com.oauth.demo.jwt.JwtUtils;
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
        log.info("app.frontend.url: {}", env.getProperty("app.frontend.url"));
        log.info("app.base.url: {}", env.getProperty("app.base.url"));
        log.info("DATABASE_URL set: {}", env.getProperty("DATABASE_URL") != null);

        String googleId = env.getProperty("spring.security.oauth2.client.registration.google.client-id", "");
        String githubId = env.getProperty("spring.security.oauth2.client.registration.github.client-id", "");
        log.info("Google OAuth configured: {}", isOAuthId(googleId));
        log.info("GitHub OAuth configured: {}", isOAuthId(githubId));

        String googleSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret", "");
        String githubSecret = env.getProperty("spring.security.oauth2.client.registration.github.client-secret", "");

        if (isOAuthId(githubId) && !isSecretConfigured(githubSecret)) {
            log.error("GITHUB_CLIENT_SECRET is NOT set on Render. GitHub OAuth will fail until you add the Client Secret from GitHub Developer Settings -> OAuth Apps -> your app -> Client secrets, then redeploy.");
        } else if (isOAuthId(githubId)) {
            log.info("GitHub OAuth client secret: configured");
        }

        if (isOAuthId(googleId) && !isSecretConfigured(googleSecret)) {
            log.error("GOOGLE_CLIENT_SECRET is NOT set on Render.");
        }

        try {
            String secret = env.getProperty("spring.app.jwtSecret", "");
            JwtUtils.deriveKeyBytes(secret);
            log.info("JWT signing key: OK");
        } catch (Exception e) {
            log.error("JWT signing key INVALID — OAuth login will fail after provider auth: {}", e.getMessage());
        }
    }

    private boolean isOAuthId(String id) {
        return id != null && !id.isBlank() && !"YOUR_CLIENT_ID".equals(id);
    }

    private boolean isSecretConfigured(String secret) {
        return secret != null && !secret.isBlank() && !"YOUR_CLIENT_SECRET".equals(secret);
    }
}
