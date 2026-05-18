package com.oauth.demo.service;

import com.oauth.demo.entity.User;
import com.oauth.demo.payload.request.SignupRequest;
import com.oauth.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    public User save(SignupRequest signupRequest) {

        User user = new User();

        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setProvider("LOCAL");
        String token = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setVerificationToken(token);
        user.setEnabled(false);

        userRepository.save(user);

        // ✅ send email
        emailService.sendVerificationEmail(user.getEmail(), token);


        return userRepository.save(user);
    }
}