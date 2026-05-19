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
        System.out.println("Request Path: " + path);
        // Skip JWT validation for public endpoints
        if (path.startsWith("/auth") ||
                path.startsWith("/user/signup") ||   // ✅ ADD THIS
                path.startsWith("/user/signin") ||   // ✅ ADD THIS
                path.startsWith("/oauth2") ||
                path.startsWith("/login/oauth2")) {
            filterChain.doFilter(request, response);
            return;

        }


        logger.debug("AuthTokenFilter called for URI: {}", request.getRequestURI());

        try {
            String jwt = parseJwt(request);//extract jwt token


            System.out.println("JWT from header = " + jwt);

            if (jwt != null && jwtUtils.validateJwtToken(jwt) &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                System.out.println("Username from token = " + username);

                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                logger.debug("Roles from JWT: {}", userDetails.getAuthorities());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
                System.out.println("AUTH SET: " + authentication);
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication in: {}", e);
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String jwt = jwtUtils.getJwtFromHeader(request);
        logger.debug("AuthTokenFilter.java: {}", jwt);
        return jwt;
    }
}
