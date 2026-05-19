package com.oauth.demo.service;

import com.oauth.demo.dto.LoginRequest;
import com.oauth.demo.dto.LoginResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthClientService {

    private final RestTemplate restTemplate = new RestTemplate();// send http request

    public LoginResponse login(String username, String password) {// This method is called when user logs in from monolith

        String url = "http://localhost:8081/auth/signin";

        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        ResponseEntity<LoginResponse> response =
                restTemplate.postForEntity(url, request, LoginResponse.class);

        return response.getBody();
    }
}