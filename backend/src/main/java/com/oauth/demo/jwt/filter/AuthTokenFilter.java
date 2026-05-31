package com.oauth.demo.jwt.filter;
import com.oauth.demo.jwt.JwtUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component//allowing spring to handle its lifecycle
public class AuthTokenFilter extends OncePerRequestFilter {//this class is used when we need to apply
    // logic only once per request

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    private static final Logger logger =
            LoggerFactory.getLogger(AuthTokenFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        String method = request.getMethod();

        if (isPublicPath(path, method)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = parseJwt(request);
            if (jwt != null) {
                if (jwtUtils.validateJwtToken(jwt)
                        && SecurityContextHolder.getContext().getAuthentication() == null) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // Invalid/expired token — treat as anonymous (do not block public routes)
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        return jwtUtils.getJwtFromHeader(request);
    }

    private boolean isPublicPath(String path, String method) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.startsWith("/auth")
                || path.startsWith("/user/signup")
                || path.startsWith("/user/signin")
                || path.startsWith("/oauth2")
                || path.startsWith("/login")
                || path.startsWith("/health")
                || path.startsWith("/oauth/status")
                || path.startsWith("/oauth/begin")
                || path.startsWith("/api/reddit")
                || (path.startsWith("/api/opportunities") && "GET".equalsIgnoreCase(method))
                || path.startsWith("/actuator")
                || path.startsWith("/h2-console")) {
            return true;
        }
        if (path.startsWith("/api/public")) {
            return true;
        }
        if (path.startsWith("/api/community") && "GET".equalsIgnoreCase(method)) {
            return true;
        }
        return path.matches("/api/community/posts/\\d+/view")
                && "POST".equalsIgnoreCase(method);
    }
}
