package com.oauth.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
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

        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            OAuth2Error error = oauth2Ex.getError();
            if (error != null) {
                log.error("OAuth2 error [{}] {} — {}",
                        type, error.getErrorCode(), error.getDescription(), exception);
            }
        } else {
            log.error("OAuth login failed [{}]: {}", type, exception.getMessage(), exception);
        }

        String code = mapErrorCode(raw, type, exception);
        String redirect = frontendUrl + "/login?error=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }

    private String mapErrorCode(String raw, String type, AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Ex && oauth2Ex.getError() != null) {
            String errorCode = oauth2Ex.getError().getErrorCode();
            if (errorCode != null) {
                return mapOAuth2ErrorCode(errorCode.toLowerCase());
            }
        }

        if (raw.contains("authorization_request_not_found")
                || raw.contains("invalid_state")
                || type.contains("AuthorizationRequestNotFound")) {
            return "session_expired";
        }
        if (raw.contains("invalid_client") || raw.contains("client secret") || raw.contains("bad credentials")) {
            return "invalid_client";
        }
        if (raw.contains("invalid_grant") || raw.contains("code_verifier") || raw.contains("pkce")) {
            return "invalid_grant";
        }
        if (raw.contains("redirect_uri")) {
            return "redirect_uri";
        }
        if (raw.contains("access_denied") || raw.contains("authorization_denied")) {
            return "access_denied";
        }
        if (raw.contains("invalid_token_response") || raw.contains("access token response")) {
            return "invalid_client";
        }

        return "oauth_failed";
    }

    private String mapOAuth2ErrorCode(String errorCode) {
        if (errorCode.contains("invalid_client")) {
            return "invalid_client";
        }
        if (errorCode.contains("invalid_grant")) {
            return "invalid_grant";
        }
        if (errorCode.contains("redirect")) {
            return "redirect_uri";
        }
        if (errorCode.contains("access_denied")) {
            return "access_denied";
        }
        if (errorCode.contains("invalid_token")) {
            return "invalid_client";
        }
        return "oauth_failed";
    }
}
