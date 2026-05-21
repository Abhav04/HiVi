package com.oauth.demo.controller;

import com.oauth.demo.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public diagnostics for OAuth deploy verification (no secrets exposed).
 */
@RestController
public class OAuthStatusController {

    private final Environment env;
    private final String frontendUrl;
    private final String baseUrl;

    public OAuthStatusController(
            Environment env,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl,
            @Value("${app.base.url:http://localhost:8080}") String baseUrl) {
        this.env = env;
        this.frontendUrl = frontendUrl;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @GetMapping("/oauth/status")
    public Map<String, Object> status() {
        String googleId = env.getProperty("spring.security.oauth2.client.registration.google.client-id", "");
        String githubId = env.getProperty("spring.security.oauth2.client.registration.github.client-id", "");
        String jwtSecret = env.getProperty("spring.app.jwtSecret", "");

        boolean googleOk = isConfigured(googleId);
        boolean githubOk = isConfigured(githubId);
        boolean jwtOk = false;
        try {
            JwtUtils.deriveKeyBytes(jwtSecret);
            jwtOk = true;
        } catch (Exception ignored) {
            jwtOk = false;
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("frontendUrl", frontendUrl);
        body.put("baseUrl", baseUrl);
        body.put("profiles", env.getActiveProfiles());
        body.put("googleOAuthConfigured", googleOk);
        body.put("githubOAuthConfigured", githubOk);
        body.put("jwtSigningKeyValid", jwtOk);
        body.put("googleRedirectUri", baseUrl + "/login/oauth2/code/google");
        body.put("githubRedirectUri", baseUrl + "/login/oauth2/code/github");
        body.put("githubClientIdPrefix", githubOk ? githubId.substring(0, Math.min(8, githubId.length())) + "..." : null);
        body.put("googleClientIdSuffix", googleOk && googleId.length() > 12
                ? "..." + googleId.substring(googleId.length() - 12) : null);
        return body;
    }

    private boolean isConfigured(String clientId) {
        return clientId != null && !clientId.isBlank() && !"YOUR_CLIENT_ID".equals(clientId);
    }
}
