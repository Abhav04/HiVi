package com.oauth.demo.controller;

import com.oauth.demo.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
        String googleSecret = env.getProperty("spring.security.oauth2.client.registration.google.client-secret", "");
        String githubSecret = env.getProperty("spring.security.oauth2.client.registration.github.client-secret", "");
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
        body.put("googleClientSecretSet", isSecretConfigured(googleSecret));
        body.put("githubClientSecretSet", isSecretConfigured(githubSecret));
        body.put("jwtSigningKeyValid", jwtOk);
        body.put("githubClientAuthentication", "client_secret_post");
        body.put("googleRedirectUri", baseUrl + "/login/oauth2/code/google");
        body.put("githubRedirectUri", baseUrl + "/login/oauth2/code/github");
        body.put("githubClientIdPrefix", githubOk ? githubId.substring(0, Math.min(8, githubId.length())) + "..." : null);
        body.put("googleClientIdSuffix", googleOk && googleId.length() > 12
                ? "..." + googleId.substring(googleId.length() - 12) : null);

        boolean githubSecretOk = isSecretConfigured(githubSecret);
        boolean googleSecretOk = isSecretConfigured(googleSecret);
        boolean readyGithub = githubOk && githubSecretOk && jwtOk;
        boolean readyGoogle = googleOk && googleSecretOk && jwtOk;

        body.put("readyForGithubLogin", readyGithub);
        body.put("readyForGoogleLogin", readyGoogle);

        List<String> issues = new ArrayList<>();
        if (githubOk && !githubSecretOk) {
            issues.add("GITHUB_CLIENT_SECRET is not set on Render. GitHub login will fail with invalid_client until you add it and redeploy.");
        }
        if (googleOk && !googleSecretOk) {
            issues.add("GOOGLE_CLIENT_SECRET is not set on Render.");
        }
        if (!jwtOk) {
            issues.add("JWT_SECRET is missing or invalid.");
        }
        body.put("issues", issues);

        return body;
    }

    private boolean isConfigured(String clientId) {
        return clientId != null && !clientId.isBlank() && !"YOUR_CLIENT_ID".equals(clientId);
    }

    private boolean isSecretConfigured(String secret) {
        return secret != null && !secret.isBlank()
                && !"YOUR_CLIENT_SECRET".equals(secret);
    }
}
