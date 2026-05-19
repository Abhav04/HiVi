package com.oauth.demo.controller;

import com.oauth.demo.dto.LoginRequest;
import com.oauth.demo.dto.LoginResponse;
import com.oauth.demo.entity.User;
import com.oauth.demo.jwt.JwtUtils;
import com.oauth.demo.payload.request.SignupRequest;
import com.oauth.demo.repository.UserRepository;
import com.oauth.demo.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // ✅ SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {

        userService.save(signUpRequest);

        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    // ✅ SIGNIN
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(item -> item.getAuthority())
                .toList();

        return ResponseEntity.ok(
                new LoginResponse(
                        userDetails.getUsername(),
                        roles,
                        jwt
                )
        );
    }

    // ✅ VERIFY
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam String email,
                                    @RequestParam String code) {

        String storedCode = redisTemplate.opsForValue().get("verify:" + email);

        if (storedCode == null) {
            return ResponseEntity.badRequest().body("Code expired");
        }

        if (!storedCode.equals(code)) {
            return ResponseEntity.badRequest().body("Invalid code");
        }

        User user = userRepository.findByEmail(email);
        user.setEnabled(true);
        userRepository.save(user);

        redisTemplate.delete("verify:" + email);

        return ResponseEntity.ok("User verified successfully");
    }
}