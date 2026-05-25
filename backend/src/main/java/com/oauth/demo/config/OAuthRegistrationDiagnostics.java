package com.oauth.demo.config;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Reads the actual Spring Security OAuth {@link ClientRegistration} beans at runtime
 * (authoritative for redirect_uri sent to Google/GitHub).
 */
@Component
public class OAuthRegistrationDiagnostics {

    private final Optional<ClientRegistrationRepository> clientRegistrationRepository;

    public OAuthRegistrationDiagnostics(
            Optional<ClientRegistrationRepository> clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    public Map<String, Object> registrationSnapshot(String registrationId) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("registrationId", registrationId);

        if (clientRegistrationRepository.isEmpty()) {
            out.put("loaded", false);
            out.put("error", "OAuth2 client auto-configuration is not active");
            return out;
        }

        ClientRegistration reg = clientRegistrationRepository.get().findByRegistrationId(registrationId);
        if (reg == null) {
            out.put("loaded", false);
            out.put("error", "No ClientRegistration for id: " + registrationId);
            return out;
        }

        String clientId = reg.getClientId() != null ? reg.getClientId().trim() : "";
        out.put("loaded", true);
        out.put("redirectUri", reg.getRedirectUri());
        out.put("authorizationUri", reg.getProviderDetails().getAuthorizationUri());
        out.put("scopes", reg.getScopes());
        out.put("clientAuthenticationMethod", reg.getClientAuthenticationMethod().getValue());
        out.put("clientIdLength", clientId.length());
        out.put("clientIdPrefix", maskPrefix(clientId, 18));
        out.put("clientIdSuffix", maskSuffix(clientId, 24));
        out.put("clientIdValidFormat", OAuthCredentialsValidator.isValidGoogleClientId(clientId)
                || OAuthCredentialsValidator.isConfiguredClientId(clientId));
        return out;
    }

    public static String maskPrefix(String value, int len) {
        if (value == null || value.isBlank()) {
            return null;
        }
        int take = Math.min(len, value.length());
        return value.substring(0, take) + (value.length() > take ? "..." : "");
    }

    public static String maskSuffix(String value, int len) {
        if (value == null || value.isBlank()) {
            return null;
        }
        int take = Math.min(len, value.length());
        return (value.length() > take ? "..." : "") + value.substring(value.length() - take);
    }
}
