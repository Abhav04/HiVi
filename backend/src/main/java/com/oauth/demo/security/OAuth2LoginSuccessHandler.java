package com.oauth.demo.security;

import com.oauth.demo.entity.User;
import com.oauth.demo.jwt.JwtUtils;
import com.oauth.demo.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        try {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

            String email = oauthUser.getAttribute("email");
            if (email == null || email.isBlank()) {
                String login = oauthUser.getAttribute("login");
                email = (login != null ? login : "user") + "@github.local";
            }

            String name = oauthUser.getAttribute("name");
            if (name == null || name.isBlank()) {
                String login = oauthUser.getAttribute("login");
                name = login != null ? login : email.split("@")[0];
            }

            String provider = email.endsWith("@github.local") ? "GITHUB" : "GOOGLE";

            User user = userRepository.findByUsername(email);
            if (user == null) {
                user = new User();
                user.setUsername(email);
                user.setEmail(email);
                user.setDisplayName(name);
                user.setProvider(provider);
                user.setRole("client");
                user.setEnabled(true);
                userRepository.save(user);
            } else {
                if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
                    user.setDisplayName(name);
                }
                if (!user.isEnabled()) {
                    user.setEnabled(true);
                }
                userRepository.save(user);
            }

            String token = jwtUtils.generateTokenFromUsername(email);

            String redirectUrl = UriComponentsBuilder
                    .fromUriString(frontendUrl + "/oauth-success")
                    .queryParam("token", token)
                    .queryParam("name", name)
                    .queryParam("email", email)
                    .queryParam("role", user.getRole() != null ? user.getRole() : "client")
                    .build()
                    .encode()
                    .toUriString();

            log.info("OAuth success for {}, redirecting to frontend", email);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            log.error("OAuth post-login processing failed", e);
            String code = "server_error";
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("jwt")) {
                code = "jwt_error";
            }
            response.sendRedirect(frontendUrl + "/login?error="
                    + URLEncoder.encode(code, StandardCharsets.UTF_8));
        }
    }
}
