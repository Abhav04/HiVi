package com.oauth.demo.service;

import com.oauth.demo.entity.User;
import com.oauth.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username);
        if (user == null && username != null && username.contains("@")) {
            user = userRepository.findByEmail(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        if (!user.isEnabled()
                && "LOCAL".equalsIgnoreCase(user.getProvider())
                && redisTemplate == null) {
            user.setEnabled(true);
            if (user.getRole() == null || user.getRole().isBlank()) {
                user.setRole("client");
            }
            user = userRepository.save(user);
        }

        String role = user.getRole() != null ? user.getRole() : "client";
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                role.startsWith("ROLE_") ? role : "ROLE_" + role
        );

        String password = user.getPassword() != null ? user.getPassword() : "{noop}";

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                password,
                user.isEnabled(),
                true,
                true,
                true,
                List.of(authority)
        );
    }
}
