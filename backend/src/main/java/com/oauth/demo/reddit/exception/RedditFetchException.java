package com.oauth.demo.reddit.exception;

public class RedditFetchException extends RuntimeException {

    private final boolean rateLimited;

    public RedditFetchException(String message, boolean rateLimited) {
        super(message);
        this.rateLimited = rateLimited;
    }

    public RedditFetchException(String message, Throwable cause, boolean rateLimited) {
        super(message, cause);
        this.rateLimited = rateLimited;
    }

    public boolean isRateLimited() {
        return rateLimited;
    }
}
