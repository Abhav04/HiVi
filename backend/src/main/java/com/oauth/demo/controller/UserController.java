package com.oauth.demo.controller;
import com.oauth.demo.entity.User;
import com.oauth.demo.dto.LoginRequest;
import com.oauth.demo.dto.LoginResponse;
import com.oauth.demo.jwt.JwtUtils;
import com.oauth.demo.payload.request.SignupRequest;
import com.oauth.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserService userService;
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test-user")
    public String testUser() {

        User user = new User();
        user.setUsername("abhav");
        user.setEmail("abhav@gmail.com");
        user.setPassword("123456");
        user.setProvider("LOCAL");

        userRepository.save(user);

        return "User saved!";
    }
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username);

        return ResponseEntity.ok(user);
    }
    @Autowired
    PasswordEncoder passwordEncoder;
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {

        // Save user
        User user = userService.save(signUpRequest);

        // Generate JWT
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user.getUsername(), null, List.of());

        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");

        return ResponseEntity.ok(response);


    }
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);



        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername());

        if (user == null) {

            user = new User();
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getUsername() + "@demo.com");
            user.setPassword(userDetails.getPassword());
            user.setProvider("LOCAL");
            user.setRole("ROLE_USER");

            userRepository.save(user);
        }
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
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String email,
                                        @RequestParam String token) {

        User user = userRepository.findByEmail(email);

        if (user == null || !token.equals(user.getVerificationToken())) {
            return ResponseEntity.badRequest().body("Invalid code");
        }

        user.setEnabled(true);
        user.setVerificationToken(null);

        userRepository.save(user);

        return ResponseEntity.ok("Email verified successfully!");
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository.findByUsername(username);

        return ResponseEntity.ok(user);
    }
}