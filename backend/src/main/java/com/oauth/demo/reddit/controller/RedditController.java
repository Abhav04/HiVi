package com.oauth.demo.reddit.controller;

import com.oauth.demo.reddit.dto.RedditTrendingResponse;
import com.oauth.demo.reddit.service.RedditTrendingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reddit")
public class RedditController {

    private final RedditTrendingService trendingService;

    public RedditController(RedditTrendingService trendingService) {
        this.trendingService = trendingService;
    }

    /**
     * Returns cached trending posts from editing-related subreddits.
     * Data is refreshed on a schedule — this endpoint does not call Reddit directly.
     */
    @GetMapping("/trending")
    public ResponseEntity<RedditTrendingResponse> getTrending(
            @RequestParam(required = false) String subreddit,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        RedditTrendingResponse response = trendingService.getTrending(subreddit, page, limit);
        return ResponseEntity.ok(response);
    }
}
