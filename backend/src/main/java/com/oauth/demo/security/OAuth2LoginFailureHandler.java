package com.oauth.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginFailureHandler.class);

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String raw = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
        String type = exception.getClass().getSimpleName();

        log.error("OAuth login failed [{}]: {}", type, exception.getMessage(), exception);

        String code = mapErrorCode(raw, type, exception);

        String redirect = frontendUrl + "/login?error=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }

    private String mapErrorCode(String raw, String type, AuthenticationException exception) {
        if (raw.contains("authorization_request_not_found")
                || raw.contains("invalid_state")
                || type.contains("AuthorizationRequestNotFound")) {
            return "session_expired";
        }
        if (raw.contains("invalid_client") || raw.contains("client secret") || raw.contains("bad credentials")) {
            return "invalid_client";
        }
        if (raw.contains("redirect_uri")) {
            return "redirect_uri";
        }
        if (raw.contains("access_denied") || raw.contains("authorization_denied")) {
            return "access_denied";
        }

        if (exception instanceof OAuth2AuthenticationException oauth2Ex
                && oauth2Ex.getError() != null
                && oauth2Ex.getError().getErrorCode() != null) {
            String errorCode = oauth2Ex.getError().getErrorCode().toLowerCase();
            if (errorCode.contains("invalid_client")) {
                return "invalid_client";
            }
            if (errorCode.contains("redirect")) {
                return "redirect_uri";
            }
        }

        return "oauth_failed";
    }
}
