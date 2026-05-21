package com.oauth.demo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Persists {@link OAuth2AuthorizationRequest} in an HTTP cookie so OAuth state survives
 * Render free-tier restarts (session-based storage is lost when the instance changes).
 */
@Component
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return getCookieValue(request)
                .map(this::deserialize)
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response);
            return;
        }
        addCookie(response, serialize(authorizationRequest));
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        deleteCookie(request, response);
        return authRequest;
    }

    private Optional<String> getCookieValue(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie == null || cookie.getValue() == null || cookie.getValue().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(cookie.getValue());
    }

    private void addCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(COOKIE_NAME, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = WebUtils.getCookie(request, COOKIE_NAME);
        if (cookie != null) {
            cookie.setValue("");
            cookie.setPath("/");
            cookie.setMaxAge(0);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }
    }

    private String serialize(OAuth2AuthorizationRequest request) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("authorizationUri", request.getAuthorizationUri());
            payload.put("clientId", request.getClientId());
            payload.put("redirectUri", request.getRedirectUri());
            payload.put("scopes", request.getScopes());
            payload.put("state", request.getState());
            payload.put("additionalParameters", request.getAdditionalParameters());
            payload.put("attributes", request.getAttributes());
            byte[] json = objectMapper.writeValueAsBytes(payload);
            return Base64.getUrlEncoder().encodeToString(json);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize OAuth2 authorization request", ex);
        }
    }

    private OAuth2AuthorizationRequest deserialize(String cookieValue) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(cookieValue);
            Map<String, Object> payload = objectMapper.readValue(json, new TypeReference<>() {});

            String authorizationUri = (String) payload.get("authorizationUri");
            String clientId = (String) payload.get("clientId");
            String redirectUri = (String) payload.get("redirectUri");
            String state = (String) payload.get("state");

            Set<String> scopes = new LinkedHashSet<>();
            Object scopesObj = payload.get("scopes");
            if (scopesObj instanceof Iterable<?> iterable) {
                for (Object item : iterable) {
                    if (item != null) {
                        scopes.add(item.toString());
                    }
                }
            }

            Map<String, Object> additionalParameters = readStringObjectMap(payload.get("additionalParameters"));
            Map<String, Object> attributes = readStringObjectMap(payload.get("attributes"));

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.authorizationCode()
                    .authorizationUri(authorizationUri)
                    .clientId(clientId)
                    .redirectUri(redirectUri)
                    .scopes(scopes)
                    .state(state);

            if (!additionalParameters.isEmpty()) {
                builder.additionalParameters(new LinkedHashMap<>(additionalParameters));
            }
            if (!attributes.isEmpty()) {
                builder.attributes(new LinkedHashMap<>(attributes));
            }

            return builder.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize OAuth2 authorization request", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readStringObjectMap(Object raw) {
        if (raw == null) {
            return Map.of();
        }
        if (raw instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> {
                if (k != null) {
                    result.put(k.toString(), v);
                }
            });
            return result;
        }
        return Map.of();
    }
}
