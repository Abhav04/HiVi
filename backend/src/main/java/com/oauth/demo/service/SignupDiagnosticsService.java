package com.oauth.demo.service;

import com.oauth.demo.entity.User;
import com.oauth.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Read-only diagnostics for signup failures — safe to call in production.
 */
@Service
public class SignupDiagnosticsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    public Map<String, Object> run() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("userCount", userRepository.count());
        out.put("redisVerificationEnabled", redisTemplate != null);
        out.put("bcryptHashLength", passwordEncoder.encode("Password123!").length());
        out.put("columns", loadUserColumns());
        out.put("insertProbe", probeInsert());
        return out;
    }

    private List<Map<String, Object>> loadUserColumns() {
        return jdbcTemplate.queryForList("""
                SELECT column_name, data_type, character_maximum_length, is_nullable
                FROM information_schema.columns
                WHERE table_schema = 'public' AND table_name = 'users'
                ORDER BY ordinal_position
                """);
    }

    public Map<String, Object> probeInsert() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "diag_" + suffix;
        String email = username + "@diag.hivi.local";

        Map<String, Object> probe = new LinkedHashMap<>();
        User saved = null;
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setDisplayName("Diag Probe");
            user.setPassword(passwordEncoder.encode("Password123!"));
            user.setProvider("LOCAL");
            user.setRole("client");
            user.setEnabled(true);

            saved = userRepository.save(user);
            probe.put("status", "OK");
            probe.put("message", "Test user insert + delete succeeded");
        } catch (Exception ex) {
            probe.put("status", "FAILED");
            probe.put("errorType", ex.getClass().getSimpleName());
            probe.put("error", AuthErrorSupport.rootMessage(ex));
        } finally {
            if (saved != null && saved.getId() != null) {
                userRepository.delete(saved);
            }
        }
        return probe;
    }
}
