package com.oauth.demo.controller;

import com.oauth.demo.service.AuthErrorSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(AuthExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", ex.getMessage() != null ? ex.getMessage() : "Invalid request"
        ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DataIntegrityViolationException ex) {
        return conflict(ex);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransaction(TransactionSystemException ex) {
        if (AuthErrorSupport.isDataIntegrity(ex)) {
            return conflict(ex);
        }
        return serverError(ex, "/auth/signup");
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadCredentials(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Unauthorized",
                "message", "Invalid email/username or password.",
                "path", "/auth/signin"
        ));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleDisabled(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "Unauthorized",
                "message", "Your account is not verified yet. Check your email for a verification code.",
                "path", "/auth/signin"
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        if (AuthErrorSupport.isDataIntegrity(ex)) {
            return conflict(ex);
        }
        return serverError(ex, "/auth/signup");
    }

    private static ResponseEntity<Map<String, Object>> conflict(Throwable ex) {
        log.warn("Signup conflict: {}", AuthErrorSupport.rootMessage(ex));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "message", "An account with this email or username already exists"
        ));
    }

    private static ResponseEntity<Map<String, Object>> serverError(Throwable ex, String path) {
        String root = AuthErrorSupport.rootMessage(ex);
        log.error("Auth error on {}: {} ({})", path, root, ex.getClass().getName(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Internal Server Error");
        body.put("message", AuthErrorSupport.userFacingSignupMessage(ex));
        body.put("detail", root);
        body.put("path", path);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
