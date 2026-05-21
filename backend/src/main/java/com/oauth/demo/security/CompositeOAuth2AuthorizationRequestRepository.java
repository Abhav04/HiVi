package com.oauth.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Stores OAuth state in both a cookie (survives Render instance restarts) and the HTTP session.
 */
@Component
public class CompositeOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final CookieOAuth2AuthorizationRequestRepository cookieRepository =
            new CookieOAuth2AuthorizationRequestRepository();
    private final HttpSessionOAuth2AuthorizationRequestRepository sessionRepository =
            new HttpSessionOAuth2AuthorizationRequestRepository();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        OAuth2AuthorizationRequest cookieRequest = cookieRepository.loadAuthorizationRequest(request);
        if (cookieRequest != null) {
            return cookieRequest;
        }
        return sessionRepository.loadAuthorizationRequest(request);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        cookieRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        sessionRepository.saveAuthorizationRequest(authorizationRequest, request, response);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                   HttpServletResponse response) {
        OAuth2AuthorizationRequest cookieRequest =
                cookieRepository.removeAuthorizationRequest(request, response);
        if (cookieRequest != null) {
            sessionRepository.removeAuthorizationRequest(request, response);
            return cookieRequest;
        }
        return sessionRepository.removeAuthorizationRequest(request, response);
    }
}
