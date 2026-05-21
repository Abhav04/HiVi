package com.oauth.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * OAuth2 client beans aligned with Spring Boot 4 / Spring Security 6 servlet APIs.
 * Uses the framework-provided session repository (no custom serialization).
 */
@Configuration
public class OAuth2ClientConfig {

    @Bean
    public AuthorizationRequestRepository<OAuth2AuthorizationRequest> oauth2AuthorizationRequestRepository() {
        return new HttpSessionOAuth2AuthorizationRequestRepository();
    }
}
