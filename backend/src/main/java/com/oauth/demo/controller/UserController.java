package com.oauth.demo.controller;
import com.oauth.demo.entity.User;
import com.oauth.demo.dto.LoginRequest;
import com.oauth.demo.dto.LoginResponse;
import com.oauth.demo.jwt.JwtUtils;
import com.oauth.demo.payload.request.SignupRequest;
import com.oauth.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Map;
import com.oauth.demo.service.UserService;
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        return ResponseEntity.ok(user);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        return ResponseEntity.ok(user);
    }
}