package com.oauth.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
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
        log.error("OAuth login failed: {}", exception.getMessage(), exception);

        String raw = exception.getMessage() != null ? exception.getMessage().toLowerCase() : "";
        String code = "oauth_failed";
        if (raw.contains("invalid_client") || raw.contains("client secret")) {
            code = "invalid_client";
        } else if (raw.contains("redirect_uri")) {
            code = "redirect_uri";
        } else if (raw.contains("access_denied") || raw.contains("authorization_denied")) {
            code = "access_denied";
        }

        String redirect = frontendUrl + "/login?error=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        response.sendRedirect(redirect);
    }
}
