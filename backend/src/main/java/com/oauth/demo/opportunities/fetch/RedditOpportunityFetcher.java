package com.oauth.demo.opportunities.fetch;

import com.oauth.demo.opportunities.config.OpportunityProperties;
import com.oauth.demo.opportunities.entity.*;
import com.oauth.demo.opportunities.service.CompanyLogoResolver;
import com.oauth.demo.opportunities.service.OpportunityClassifier;
import com.oauth.demo.opportunities.service.OpportunityRanker;
import com.oauth.demo.reddit.dto.RedditPostDto;
import com.oauth.demo.reddit.exception.RedditFetchException;
import com.oauth.demo.reddit.service.RedditApiClient;
import com.oauth.demo.reddit.service.RedditHiringDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RedditOpportunityFetcher {

    private static final Logger log = LoggerFactory.getLogger(RedditOpportunityFetcher.class);

    private final RedditApiClient redditApiClient;
    private final OpportunityProperties properties;
    private final CompanyLogoResolver logoResolver;

    public RedditOpportunityFetcher(
            RedditApiClient redditApiClient,
            OpportunityProperties properties,
            CompanyLogoResolver logoResolver) {
        this.redditApiClient = redditApiClient;
        this.properties = properties;
        this.logoResolver = logoResolver;
    }

    public List<Opportunity> fetchHiringPosts() {
        List<Opportunity> results = new ArrayList<>();

        for (String subreddit : properties.getRedditHiringSubreddits()) {
            try {
                List<RedditPostDto> posts = redditApiClient.fetchHotPosts(subreddit);
                for (RedditPostDto post : posts) {
                    String sub = subreddit;
                    String selftext = "";
                    if (!RedditHiringDetector.isHiringPost(post.title(), sub, selftext)) {
                        continue;
                    }
                    results.add(mapPost(post, sub));
                }
            } catch (RedditFetchException ex) {
                log.warn("Reddit hiring fetch skipped for r/{}: {}", subreddit, ex.getMessage());
                if (ex.isRateLimited()) {
                    break;
                }
            }
        }

        return results;
    }

    private Opportunity mapPost(RedditPostDto post, String subreddit) {
        String title = post.title();
        String description = truncate(title, 280);
        List<String> badges = OpportunityClassifier.resolveBadges(title, "", subreddit);
        OpportunityCategory category = OpportunityClassifier.classifyCategory(title, "");
        WorkMode workMode = OpportunityClassifier.classifyWorkMode(title, "");
        String pay = OpportunityClassifier.extractPayLabel(title, "");

        Opportunity o = new Opportunity();
        o.setSource(OpportunitySource.REDDIT);
        o.setExternalId(post.id());
        o.setTitle(title);
        o.setCompany(CompanyLogoResolver.parseClientCompany(title, subreddit));
        o.setDescription(description);
        o.setApplyUrl(post.redditUrl() != null ? post.redditUrl() : post.permalink());
        o.setPayLabel(pay);
        o.setWorkMode(workMode);
        o.setCategory(category);
        o.setTags(String.join(",", OpportunityClassifier.buildTags(category)));
        o.setBadges(badges.stream().collect(Collectors.joining(",")));
        o.setPostedAt(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(post.createdUtc()), ZoneId.systemDefault()));
        o.setEngagementCount(post.upvotes() + post.commentCount());
        OpportunityRanker.refreshTrendingScore(o, post.upvotes(), post.commentCount());
        o.setActive(true);
        logoResolver.applyTo(o);
        return o;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 3) + "...";
    }
}
