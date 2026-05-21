package com.oauth.demo.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.util.WebUtils;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Cookie-backed OAuth2 authorization request store for Render cold starts.
 */
public class CookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger log =
            LoggerFactory.getLogger(CookieOAuth2AuthorizationRequestRepository.class);

    static final String COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 600;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        try {
            return getCookieValue(request)
                    .map(this::deserialize)
                    .orElse(null);
        } catch (Exception ex) {
            log.warn("Could not restore OAuth2 authorization request from cookie: {}", ex.getMessage());
            return null;
        }
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(request, response);
            return;
        }
        try {
            addCookie(response, serialize(authorizationRequest));
        } catch (Exception ex) {
            log.error("Failed to persist OAuth2 authorization request cookie", ex);
        }
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
            payload.put("authorizationGrantType", request.getAuthorizationGrantType().getValue());
            payload.put("additionalParameters", stringifyMap(request.getAdditionalParameters()));
            payload.put("attributes", stringifyMap(request.getAttributes()));
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
            String grantType = (String) payload.getOrDefault(
                    "authorizationGrantType", AuthorizationGrantType.AUTHORIZATION_CODE.getValue());

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

            OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest
                    .authorizationCode()
                    .authorizationUri(authorizationUri)
                    .clientId(clientId)
                    .redirectUri(redirectUri)
                    .scopes(scopes)
                    .state(state)
                    .authorizationGrantType(new AuthorizationGrantType(grantType));

            if (!additionalParameters.isEmpty()) {
                builder.additionalParameters(additionalParameters);
            }
            if (!attributes.isEmpty()) {
                builder.attributes(attributes);
            }

            return builder.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize OAuth2 authorization request", ex);
        }
    }

    private Map<String, Object> stringifyMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (source == null) {
            return result;
        }
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                result.put(key, value.toString());
            }
        });
        return result;
    }

    private Map<String, Object> readStringObjectMap(Object raw) {
        if (raw == null) {
            return new LinkedHashMap<>();
        }
        if (raw instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> {
                if (k != null && v != null) {
                    result.put(k.toString(), v.toString());
                }
            });
            return result;
        }
        return new LinkedHashMap<>();
    }
}
