package com.oauth.demo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Extends the default OAuth2 user loader to resolve GitHub primary email when not public.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);

        if (!"github".equals(userRequest.getClientRegistration().getRegistrationId())) {
            return oauthUser;
        }

        if (oauthUser.getAttribute("email") != null) {
            return oauthUser;
        }

        String login = oauthUser.getAttribute("login") != null
                ? oauthUser.getAttribute("login").toString()
                : "github-user";

        try {
            String token = userRequest.getAccessToken().getTokenValue();
            RequestEntity<Void> request = RequestEntity
                    .get(URI.create("https://api.github.com/user/emails"))
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .build();

            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    request,
                    new ParameterizedTypeReference<>() {}
            );

            List<Map<String, Object>> emails = response.getBody();
            if (emails != null) {
                for (Map<String, Object> entry : emails) {
                    Boolean primary = (Boolean) entry.get("primary");
                    Boolean verified = (Boolean) entry.get("verified");
                    Object email = entry.get("email");
                    if (email != null && Boolean.TRUE.equals(verified) && Boolean.TRUE.equals(primary)) {
                        Map<String, Object> attributes = new LinkedHashMap<>(oauthUser.getAttributes());
                        attributes.put("email", email.toString());
                        log.info("Resolved GitHub primary email for user {}", login);
                        return new DefaultOAuth2User(
                                oauthUser.getAuthorities(),
                                attributes,
                                "login"
                        );
                    }
                }
            }
        } catch (Exception ex) {
            log.warn("Could not fetch GitHub email list for {}: {}", login, ex.getMessage());
        }

        return oauthUser;
    }
}
