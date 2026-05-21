package com.oauth.demo.reddit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "com.oauth.demo.reddit")
public class RedditExceptionHandler {

    @ExceptionHandler(RedditFetchException.class)
    public ResponseEntity<Map<String, Object>> handleRedditFetch(RedditFetchException ex) {
        HttpStatus status = ex.isRateLimited() ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(Map.of(
                "error", "reddit_fetch_failed",
                "message", ex.getMessage(),
                "rateLimited", ex.isRateLimited()
        ));
    }
}
