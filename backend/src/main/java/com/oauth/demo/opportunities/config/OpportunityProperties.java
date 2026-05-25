package com.oauth.demo.opportunities.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "opportunities")
public class OpportunityProperties {

    private boolean enabled = true;
    private long refreshIntervalMs = 900_000;
    private int feedPageSize = 20;
    private List<String> redditHiringSubreddits = new ArrayList<>(List.of(
            "VideoEditor_forhire",
            "forhire",
            "FreelanceIndia",
            "hireaneditor",
            "VideoEditors"
    ));

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public long getRefreshIntervalMs() { return refreshIntervalMs; }
    public void setRefreshIntervalMs(long refreshIntervalMs) { this.refreshIntervalMs = refreshIntervalMs; }
    public int getFeedPageSize() { return feedPageSize; }
    public void setFeedPageSize(int feedPageSize) { this.feedPageSize = feedPageSize; }
    public List<String> getRedditHiringSubreddits() { return redditHiringSubreddits; }
    public void setRedditHiringSubreddits(List<String> redditHiringSubreddits) {
        this.redditHiringSubreddits = redditHiringSubreddits;
    }
}
