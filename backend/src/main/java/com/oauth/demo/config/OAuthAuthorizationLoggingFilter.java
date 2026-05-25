package com.oauth.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Logs the OAuth redirect URI and client id prefix when an authorization redirect starts.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class OAuthAuthorizationLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthAuthorizationLoggingFilter.class);

    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

    public OAuthAuthorizationLoggingFilter(
            Optional<ClientRegistrationRepository> clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/oauth2/authorization/")) {
            String provider = uri.substring("/oauth2/authorization/".length());
            logOAuthStart(request, provider);
        } else if (uri != null && uri.startsWith("/oauth/begin")) {
            log.info(
                    "OAuth begin: provider={} frontend={} remote={} host={}",
                    request.getParameter("provider"),
                    request.getParameter("frontend"),
                    request.getRemoteAddr(),
                    request.getHeader("Host"));
        }

        filterChain.doFilter(request, response);
    }

    private void logOAuthStart(HttpServletRequest request, String provider) {
        if (clientRegistrationRepository.isEmpty()) {
            log.warn("OAuth authorization for '{}' but ClientRegistrationRepository is missing", provider);
            return;
        }
        ClientRegistration reg = clientRegistrationRepository.get().findByRegistrationId(provider);
        if (reg == null) {
            log.warn("OAuth authorization for unknown provider '{}'", provider);
            return;
        }
        log.info(
                "OAuth authorization START provider={} redirectUri={} clientIdPrefix={} requestHost={} requestUrl={}",
                provider,
                reg.getRedirectUri(),
                OAuthRegistrationDiagnostics.maskPrefix(reg.getClientId(), 18),
                request.getHeader("Host"),
                request.getRequestURL());
    }
}
