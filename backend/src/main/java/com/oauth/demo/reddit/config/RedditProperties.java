package com.oauth.demo.reddit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "reddit")
public class RedditProperties {

    private String baseUrl = "https://www.reddit.com";
    private String userAgent = "HiVi/1.0 (Spring Boot; +https://hi-vi.vercel.app)";
    private List<String> subreddits = List.of(
            "videoediting", "editors", "AfterEffects", "premiere", "videography", "Filmmakers"
    );
    private int postsPerSubreddit = 8;
    private long refreshIntervalMs = 300_000L;
    private int connectTimeoutMs = 5_000;
    private int readTimeoutMs = 10_000;
    private long requestDelayMs = 1_100L;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public void setSubreddits(List<String> subreddits) {
        this.subreddits = subreddits;
    }

    public int getPostsPerSubreddit() {
        return postsPerSubreddit;
    }

    public void setPostsPerSubreddit(int postsPerSubreddit) {
        this.postsPerSubreddit = postsPerSubreddit;
    }

    public long getRefreshIntervalMs() {
        return refreshIntervalMs;
    }

    public void setRefreshIntervalMs(long refreshIntervalMs) {
        this.refreshIntervalMs = refreshIntervalMs;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public long getRequestDelayMs() {
        return requestDelayMs;
    }

    public void setRequestDelayMs(long requestDelayMs) {
        this.requestDelayMs = requestDelayMs;
    }
}
