package com.oauth.demo.config;

/**
 * Detects placeholder / invalid OAuth client credentials before redirecting to Google/GitHub.
 */
public final class OAuthCredentialsValidator {

    private OAuthCredentialsValidator() {}

    public static boolean isPlaceholderClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return true;
        }
        String id = clientId.trim();
        if ("YOUR_CLIENT_ID".equals(id)) {
            return true;
        }
        String lower = id.toLowerCase();
        return lower.startsWith("your-")
                || lower.contains("example")
                || lower.contains("placeholder")
                || lower.endsWith("-client-id");
    }

    public static boolean isPlaceholderSecret(String secret) {
        if (secret == null || secret.isBlank()) {
            return true;
        }
        String s = secret.trim();
        if ("YOUR_CLIENT_SECRET".equals(s)) {
            return true;
        }
        String lower = s.toLowerCase();
        return lower.startsWith("your-")
                || lower.contains("example")
                || lower.contains("placeholder")
                || lower.endsWith("-client-secret");
    }

    public static boolean isValidGoogleClientId(String clientId) {
        if (isPlaceholderClientId(clientId)) {
            return false;
        }
        return clientId.trim().endsWith(".apps.googleusercontent.com");
    }

    public static boolean isConfiguredClientId(String clientId) {
        return !isPlaceholderClientId(clientId);
    }

    public static boolean isConfiguredSecret(String secret) {
        return !isPlaceholderSecret(secret);
    }
}
