package com.oauth.demo.security;

import com.oauth.demo.entity.User;
import com.oauth.demo.jwt.JwtUtils;
import com.oauth.demo.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        if (email == null) {
            email = oauthUser.getAttribute("login") + "@github.local";
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
        } else if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            user.setDisplayName(name);
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

        response.sendRedirect(redirectUrl);
    }
}
