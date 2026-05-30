package com.oauth.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Eager-loads OAuth client registrations at startup so the first login is not delayed
 * by lazy-init + ClientRegistration resolution on the callback path.
 */
@Component
public class OAuthStartupWarmup {

    private static final Logger log = LoggerFactory.getLogger(OAuthStartupWarmup.class);

    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;
    private final Environment env;

    public OAuthStartupWarmup(
            Optional<ClientRegistrationRepository> clientRegistrationRepository,
            Environment env) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmOAuthRegistrations() {
        if (clientRegistrationRepository.isEmpty()) {
            log.warn("OAuth2 ClientRegistrationRepository not available — social login disabled");
            return;
        }

        ClientRegistrationRepository repo = clientRegistrationRepository.get();
        for (String id : new String[] { "google", "github" }) {
            ClientRegistration reg = repo.findByRegistrationId(id);
            if (reg != null) {
                log.info("OAuth [{}] redirect_uri={} (register this EXACT URL in {} console)",
                        id,
                        reg.getRedirectUri(),
                        "google".equals(id) ? "Google Cloud" : "GitHub");
            }
        }

        String base = env.getProperty("app.base.url", "");
        if (base.endsWith("/")) {
            log.error("app.base.url ends with '/' — this can cause redirect_uri_mismatch. "
                    + "Set APP_BASE_URL without trailing slash on Render.");
        }
    }
}
