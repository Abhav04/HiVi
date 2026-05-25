package com.oauth.demo.controller;

import com.oauth.demo.config.OAuthCredentialsValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * Starts OAuth with an optional frontend return URL (stored in session for the success redirect).
 */
@Controller
public class OAuthBeginController {

    public static final String SESSION_FRONTEND_RETURN = "OAUTH_FRONTEND_RETURN_URL";

    private static final Set<String> ALLOWED_PROVIDERS = Set.of("google", "github");

    private final Environment env;
    private final String defaultFrontendUrl;

    public OAuthBeginController(
            Environment env,
            @Value("${app.frontend.url:http://localhost:3000}") String defaultFrontendUrl) {
        this.env = env;
        this.defaultFrontendUrl = defaultFrontendUrl;
    }

    @GetMapping("/oauth/begin")
    public void begin(
            @RequestParam String provider,
            @RequestParam(required = false) String frontend,
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session) throws IOException {

        String normalized = provider == null ? "" : provider.trim().toLowerCase();
        if (!ALLOWED_PROVIDERS.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown OAuth provider");
        }

        validateProviderReady(normalized);

        String returnFrontend = resolveFrontendUrl(frontend);
        session.setAttribute(SESSION_FRONTEND_RETURN, returnFrontend);

        response.sendRedirect(request.getContextPath() + "/oauth2/authorization/" + normalized);
    }

    private void validateProviderReady(String provider) {
        if ("google".equals(provider)) {
            String id = env.getProperty("spring.security.oauth2.client.registration.google.client-id", "");
            String secret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret", "");
            if (!OAuthCredentialsValidator.isValidGoogleClientId(id)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Google OAuth client ID is missing or still a placeholder. "
                                + "Set GOOGLE_CLIENT_ID in backend/local.env (must end with .apps.googleusercontent.com).");
            }
            if (!OAuthCredentialsValidator.isConfiguredSecret(secret)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Google client secret is not configured.");
            }
            return;
        }

        String id = env.getProperty("spring.security.oauth2.client.registration.github.client-id", "");
        String secret = env.getProperty("spring.security.oauth2.client.registration.github.client-secret", "");
        if (!OAuthCredentialsValidator.isConfiguredClientId(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GitHub client ID is not configured.");
        }
        if (!OAuthCredentialsValidator.isConfiguredSecret(secret)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "GitHub client secret is not configured.");
        }
    }

    private String resolveFrontendUrl(String frontend) {
        if (frontend == null || frontend.isBlank()) {
            return defaultFrontendUrl;
        }
        try {
            URI uri = URI.create(frontend.trim());
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                return defaultFrontendUrl;
            }
            String host = uri.getHost();
            if (host == null) {
                return defaultFrontendUrl;
            }
            boolean local = "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host);
            boolean vercel = host.endsWith(".vercel.app") || host.equals("hi-vi.vercel.app");
            if (!local && !vercel) {
                return defaultFrontendUrl;
            }
            return uri.getScheme() + "://" + uri.getAuthority();
        } catch (Exception e) {
            return defaultFrontendUrl;
        }
    }
}
