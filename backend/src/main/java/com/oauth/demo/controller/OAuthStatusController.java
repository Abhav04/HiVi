package com.oauth.demo.controller;

import com.oauth.demo.config.OAuthCredentialsValidator;
import com.oauth.demo.config.OAuthRegistrationDiagnostics;
import com.oauth.demo.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Public diagnostics for OAuth deploy verification (no secrets exposed).
 */
@RestController
public class OAuthStatusController {

    private final Environment env;
    private final OAuthRegistrationDiagnostics registrationDiagnostics;
    private final String frontendUrl;
    private final String baseUrl;

    public OAuthStatusController(
            Environment env,
            OAuthRegistrationDiagnostics registrationDiagnostics,
            @Value("${app.frontend.url:http://localhost:3000}") String frontendUrl,
            @Value("${app.base.url:http://localhost:8080}") String baseUrl) {
        this.env = env;
        this.registrationDiagnostics = registrationDiagnostics;
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

        boolean googleOk = OAuthCredentialsValidator.isValidGoogleClientId(googleId);
        boolean githubOk = OAuthCredentialsValidator.isConfiguredClientId(githubId);
        boolean googlePlaceholder = OAuthCredentialsValidator.isPlaceholderClientId(googleId)
                || OAuthCredentialsValidator.isPlaceholderSecret(googleSecret);
        boolean githubPlaceholder = OAuthCredentialsValidator.isPlaceholderClientId(githubId)
                || OAuthCredentialsValidator.isPlaceholderSecret(githubSecret);
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
        body.put("googleClientSecretSet", OAuthCredentialsValidator.isConfiguredSecret(googleSecret));
        body.put("githubClientSecretSet", OAuthCredentialsValidator.isConfiguredSecret(githubSecret));
        body.put("oauthPlaceholdersDetected", googlePlaceholder || githubPlaceholder);
        body.put("jwtSigningKeyValid", jwtOk);
        body.put("githubClientAuthentication", "client_secret_post");
        String configuredGoogleRedirect = env.getProperty(
                "spring.security.oauth2.client.registration.google.redirect-uri", "");
        String configuredGithubRedirect = env.getProperty(
                "spring.security.oauth2.client.registration.github.redirect-uri", "");

        Map<String, Object> googleRuntime = registrationDiagnostics.registrationSnapshot("google");
        Map<String, Object> githubRuntime = registrationDiagnostics.registrationSnapshot("github");

        String runtimeGoogleRedirect = (String) googleRuntime.get("redirectUri");
        String runtimeGithubRedirect = (String) githubRuntime.get("redirectUri");

        body.put("googleRedirectUri", runtimeGoogleRedirect != null ? runtimeGoogleRedirect : baseUrl + "/login/oauth2/code/google");
        body.put("githubRedirectUri", runtimeGithubRedirect != null ? runtimeGithubRedirect : baseUrl + "/login/oauth2/code/github");
        body.put("googleRedirectUriConfigured", configuredGoogleRedirect);
        body.put("githubRedirectUriConfigured", configuredGithubRedirect);
        body.put("googleRegistration", googleRuntime);
        body.put("githubRegistration", githubRuntime);
        body.put("githubClientIdPrefix", githubRuntime.get("clientIdPrefix"));
        body.put("googleClientIdPrefix", googleRuntime.get("clientIdPrefix"));
        body.put("googleClientIdSuffix", googleRuntime.get("clientIdSuffix"));

        boolean localProfile = Arrays.asList(env.getActiveProfiles()).contains("local");
        body.put("recommendedGoogleRedirectUris", List.of(
                "http://localhost:8080/login/oauth2/code/google",
                "https://hivi-idam.onrender.com/login/oauth2/code/google"));
        body.put("recommendedGithubRedirectUris", List.of(
                "http://localhost:8080/login/oauth2/code/github",
                "https://hivi-idam.onrender.com/login/oauth2/code/github"));
        body.put("recommendedJavaScriptOrigins", List.of(
                "http://localhost:3000",
                "http://localhost:8080",
                "https://hi-vi.vercel.app"));
        body.put("oauthBeginUrl", baseUrl + "/oauth/begin?provider=google&frontend=" + frontendUrl);
        body.put("oauthAuthorizationProbeUrl", baseUrl + "/oauth2/authorization/google");

        boolean githubSecretOk = OAuthCredentialsValidator.isConfiguredSecret(githubSecret);
        boolean googleSecretOk = OAuthCredentialsValidator.isConfiguredSecret(googleSecret);
        boolean readyGithub = githubOk && githubSecretOk && jwtOk;
        boolean readyGoogle = googleOk && googleSecretOk && jwtOk;

        body.put("readyForGithubLogin", readyGithub);
        body.put("readyForGoogleLogin", readyGoogle);

        List<String> issues = new ArrayList<>();
        if (googlePlaceholder) {
            issues.add(
                    "GOOGLE_CLIENT_ID/SECRET in backend/local.env are still example placeholders. "
                            + "Copy real values from Render → Environment (client ID must end with .apps.googleusercontent.com).");
        }
        if (githubPlaceholder) {
            issues.add(
                    "GITHUB_CLIENT_ID/SECRET in backend/local.env are still example placeholders. Copy real values from Render → Environment.");
        }
        if (githubOk && !githubSecretOk) {
            issues.add("GITHUB_CLIENT_SECRET is not set on Render. GitHub login will fail with invalid_client until you add it and redeploy.");
        }
        if (googleOk && !googleSecretOk) {
            issues.add("GOOGLE_CLIENT_SECRET is not set on Render.");
        }
        if (!jwtOk) {
            issues.add("JWT_SECRET is missing or invalid.");
        }

        if (runtimeGoogleRedirect != null && configuredGoogleRedirect != null
                && !configuredGoogleRedirect.isBlank()
                && !runtimeGoogleRedirect.equals(configuredGoogleRedirect.trim())) {
            issues.add("Config drift: property google.redirect-uri does not match runtime ClientRegistration redirectUri.");
        }

        if (localProfile && googleOk) {
            issues.add(
                    "redirect_uri_mismatch fix: In Google Cloud Console open the OAuth client whose Client ID starts with "
                            + googleRuntime.get("clientIdPrefix")
                            + " (same as GOOGLE_CLIENT_ID in local.env) and add BOTH redirect URIs from recommendedGoogleRedirectUris.");
        }

        body.put("issues", issues);

        return body;
    }

}
