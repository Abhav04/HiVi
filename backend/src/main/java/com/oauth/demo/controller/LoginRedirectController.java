package com.oauth.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Redirects browser hits on backend /login (e.g. OAuth error) to the React frontend.
 */
@Controller
public class LoginRedirectController {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @GetMapping("/login")
    public String redirectToFrontend(@RequestParam(required = false) String error) {
        if (error != null && !error.isBlank()) {
            return "redirect:" + frontendUrl + "/login?error="
                    + URLEncoder.encode(error, StandardCharsets.UTF_8);
        }
        return "redirect:" + frontendUrl + "/login";
    }
}
