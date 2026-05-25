package com.oauth.demo.community.service;

import com.oauth.demo.entity.User;
import com.oauth.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class CommunityUserService {

    private final UserRepository userRepository;

    public CommunityUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication required");
        }
        User user = userRepository.findByUsername(authentication.getName());
        if (user == null) {
            throw new IllegalStateException("User not found");
        }
        return user;
    }

    public User optionalUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName());
    }
}
