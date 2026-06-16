package com.oauth.demo.service;

import com.oauth.demo.entity.User;
import com.oauth.demo.payload.request.SignupRequest;
import com.oauth.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private EmailService emailService;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Transactional
    public User save(SignupRequest signupRequest) {
        String email = normalize(signupRequest.getEmail());
        String username = normalize(signupRequest.getUsername());
        String password = signupRequest.getPassword();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        if (userRepository.findByEmail(email) != null) {
            throw new IllegalArgumentException("An account with this email already exists");
        }
        if (userRepository.findByUsername(username) != null) {
            throw new IllegalArgumentException("This username is already taken");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(trimToNull(signupRequest.getDisplayName()));
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider("LOCAL");

        String role = signupRequest.getRole();
        user.setRole(role != null && !role.isBlank() ? role.toLowerCase() : "client");

        boolean verificationEnabled = redisTemplate != null;
        user.setEnabled(!verificationEnabled);

        user = userRepository.save(user);
        log.info("Persisted local user id={} username={}", user.getId(), user.getUsername());

        if (verificationEnabled) {
            String token = String.valueOf((int) (Math.random() * 900000) + 100000);
            redisTemplate.opsForValue().set(
                    "verify:" + user.getEmail(),
                    token,
                    java.time.Duration.ofMinutes(5)
            );
            if (emailService != null) {
                emailService.sendVerificationEmail(user.getEmail(), token);
            }
        }

        return user;
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
