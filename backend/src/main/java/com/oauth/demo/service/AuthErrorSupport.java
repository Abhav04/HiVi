package com.oauth.demo.service;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

final class AuthErrorSupport {

    private AuthErrorSupport() {}

    static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    static String rootMessage(Throwable throwable) {
        if (throwable == null) {
            return "Unknown error";
        }
        Throwable root = rootCause(throwable);
        String message = root.getMessage();
        if (message != null && !message.isBlank()) {
            return message;
        }
        return throwable.getMessage() != null ? throwable.getMessage() : root.getClass().getSimpleName();
    }

    static boolean isDataIntegrity(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof DataIntegrityViolationException) {
                return true;
            }
            if (current instanceof SQLException sqlEx && "23505".equals(sqlEx.getSQLState())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    static String userFacingSignupMessage(Throwable throwable) {
        if (isDataIntegrity(throwable)) {
            return "An account with this email or username already exists";
        }
        String root = rootMessage(throwable);
        if (root.toLowerCase().contains("value too long")
                || root.toLowerCase().contains("too long for type")) {
            return "Account could not be created because the database password column is too short. Run DB migration to widen users.password.";
        }
        if (root.toLowerCase().contains("null value")
                || root.toLowerCase().contains("not-null")) {
            return "Account could not be created due to a database schema mismatch (missing required field).";
        }
        return "Signup failed due to a server error. Please retry in a moment.";
    }
}
