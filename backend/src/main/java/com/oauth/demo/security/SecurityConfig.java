package com.oauth.demo.security;

import com.oauth.demo.jwt.filter.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private AuthTokenFilter authenticationJwtTokenFilter;

    private final OAuth2LoginSuccessHandler successHandler;
    private final OAuth2LoginFailureHandler failureHandler;
    private final AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private final CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(
            OAuth2LoginSuccessHandler successHandler,
            OAuth2LoginFailureHandler failureHandler,
            AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository,
            CustomOAuth2UserService customOAuth2UserService) {
        this.successHandler = successHandler;
        this.failureHandler = failureHandler;
        this.authorizationRequestRepository = authorizationRequestRepository;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, Environment env) throws Exception {
        http.securityMatcher("/**");
        http.cors(cors -> {});
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);

        // Required for OAuth2 authorization request + authorized client between redirects
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));

        http.exceptionHandling(exception ->
                exception.authenticationEntryPoint(unauthorizedHandler));

        http.authorizeHttpRequests(auth ->
                auth.requestMatchers("/user/signin").permitAll()
                        .requestMatchers("/user/signup").permitAll()
                        .requestMatchers("/auth/signin").permitAll()
                        .requestMatchers("/auth/signup").permitAll()
                        .requestMatchers("/user/test-user").permitAll()
                        .requestMatchers("/login", "/login/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/posts/**").permitAll()
                        .requestMatchers("/health", "/actuator/health", "/oauth/status").permitAll()
                        .requestMatchers("/api/reddit/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated());

        if (isOAuthConfigured(env)) {
            http.oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint -> endpoint
                            .authorizationRequestRepository(authorizationRequestRepository))
                    .userInfoEndpoint(userInfo -> userInfo
                            .userService(customOAuth2UserService))
                    .successHandler(successHandler)
                    .failureHandler(failureHandler));
        }

        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        http.addFilterBefore(authenticationJwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private boolean isOAuthConfigured(Environment env) {
        String googleId = env.getProperty("spring.security.oauth2.client.registration.google.client-id", "");
        String githubId = env.getProperty("spring.security.oauth2.client.registration.github.client-id", "");
        return (!googleId.isBlank() && !"YOUR_CLIENT_ID".equals(googleId))
                || (!githubId.isBlank() && !"YOUR_CLIENT_ID".equals(githubId));
    }
}
