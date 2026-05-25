package com.oauth.demo.reddit.service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Detects hiring / freelance opportunities in Reddit post titles and bodies.
 */
public final class RedditHiringDetector {

    private static final Set<String> HIRING_SUBREDDITS = Set.of(
            "videoeditor_forhire",
            "freelanceindia",
            "forhire",
            "hireavideographer",
            "hireaneditor"
    );

    private static final Pattern[] HIRING_PATTERNS = {
            Pattern.compile("\\b(hiring|hire)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\blooking\\s+for\\s+(an?\\s+)?(video\\s+)?editor", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bneed(s)?\\s+(a\\s+)?(video\\s+)?editor", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(paid\\s+work|paid\\s+gig|paid\\s+project)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(freelance|client\\s+work)\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\breel\\s+editor\\s+needed\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bediting\\s+job\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b\\[for\\s+hire\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b\\[hiring\\]", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bseeking\\s+(video\\s+)?editor", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\b(editor|video\\s+editor)\\s+wanted\\b", Pattern.CASE_INSENSITIVE),
    };

    private static final Pattern URGENT_PATTERN = Pattern.compile(
            "\\b(urgent|asap|immediate|rush|deadline\\s+today)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PAID_PATTERN = Pattern.compile(
            "\\b(paid|\\$|₹|budget|rate|compensation|invoice)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private RedditHiringDetector() {}

    public static boolean isHiringPost(String title, String subreddit, String selftext) {
        String combined = (title + " " + (selftext == null ? "" : selftext)).toLowerCase(Locale.ROOT);
        String sub = normalizeSubreddit(subreddit);

        if (HIRING_SUBREDDITS.contains(sub)) {
            return true;
        }

        for (Pattern pattern : HIRING_PATTERNS) {
            if (pattern.matcher(combined).find()) {
                return true;
            }
        }
        return false;
    }

    public static String resolveBadge(String title, String subreddit, String selftext) {
        if (!isHiringPost(title, subreddit, selftext)) {
            return null;
        }

        String combined = (title + " " + (selftext == null ? "" : selftext)).toLowerCase(Locale.ROOT);

        if (URGENT_PATTERN.matcher(combined).find()) {
            return "Urgent";
        }
        if (PAID_PATTERN.matcher(combined).find()) {
            return "Paid";
        }
        if (normalizeSubreddit(subreddit).contains("forhire")
                || normalizeSubreddit(subreddit).contains("freelance")) {
            return "Freelance";
        }
        return "Hiring";
    }

    public static double hiringBoost(String title, String subreddit, String selftext) {
        if (!isHiringPost(title, subreddit, selftext)) {
            return 0;
        }
        double boost = 35.0;
        String sub = normalizeSubreddit(subreddit);
        if (HIRING_SUBREDDITS.contains(sub)) {
            boost += 25.0;
        }
        if (URGENT_PATTERN.matcher(title.toLowerCase(Locale.ROOT)).find()) {
            boost += 15.0;
        }
        return boost;
    }

    private static String normalizeSubreddit(String subreddit) {
        if (subreddit == null) {
            return "";
        }
        return subreddit.toLowerCase(Locale.ROOT).replace("r/", "").trim();
    }
}
