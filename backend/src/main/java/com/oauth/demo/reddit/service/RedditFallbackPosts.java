package com.oauth.demo.reddit.service;

import com.oauth.demo.reddit.dto.RedditPostDto;

import java.time.Instant;
import java.util.List;

/**
 * Curated fallback when Reddit's public API blocks the server (common on cloud hosts).
 */
public final class RedditFallbackPosts {

    private RedditFallbackPosts() {}

    public static List<RedditPostDto> curated() {
        long now = Instant.now().getEpochSecond();
        return List.of(
                post("fb1", "Looking for a Premiere editor for weekly YouTube uploads",
                        "VideoEditor_forhire", "hire_client", 142, 38,
                        "https://images.unsplash.com/photo-1598488035139-bdbb2231d1bb?w=800&q=80",
                        now - 7200, true, "Hiring"),
                post("fb2", "My color grading workflow for documentary projects (DaVinci)",
                        "videoediting", "grade_pro", 891, 124,
                        "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80",
                        now - 14400, false, null),
                post("fb3", "[Paid] Need motion graphics intro for tech startup reel",
                        "forhire", "startup_pm", 256, 41,
                        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                        now - 21600, true, "Paid"),
                post("fb4", "Before/after: anime fight scene compositing breakdown",
                        "VideoEditors", "anime_editor_kai", 1204, 89,
                        "https://images.unsplash.com/photo-1612036789805-4b12b83956c4?w=800&q=80",
                        now - 28800, false, null),
                post("fb5", "Freelance video editor available — corporate & social",
                        "FreelanceIndia", "dev_edits", 178, 52,
                        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&q=80",
                        now - 36000, true, "Available"),
                post("fb6", "Best export settings for Instagram Reels in 2025?",
                        "premiere", "reel_maker", 445, 167,
                        "https://images.unsplash.com/photo-1611532736597-de2d90e850f7?w=800&q=80",
                        now - 43200, false, null),
                post("fb7", "Cinematic travel vlog — feedback on pacing appreciated",
                        "Filmmakers", "travel_cut", 623, 71,
                        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&q=80",
                        now - 50400, false, null),
                post("fb8", "After Effects tip: smooth speed ramps without robotic motion",
                        "AfterEffects", "motion_leo", 512, 93,
                        "https://images.unsplash.com/photo-1478720568477-152d9b8e6839?w=800&q=80",
                        now - 57600, false, null),
                post("fb9", "Hiring gaming montage editor — Valorant highlights channel",
                        "VideoEditor_forhire", "esports_host", 334, 45,
                        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80",
                        now - 64800, true, "Hiring"),
                post("fb10", "Client showcase: 90s fintech explainer with icon animation",
                        "videoediting", "motion_studio", 389, 31,
                        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&q=80",
                        now - 72000, false, null),
                post("fb11", "Wedding filmmaker — how do you deliver same-day teasers?",
                        "videography", "wedding_pro", 267, 88,
                        "https://images.unsplash.com/photo-1519741497674-611481863552?w=800&q=80",
                        now - 79200, false, null),
                post("fb12", "Remote editor for podcast video — $40/hr, long-term",
                        "forhire", "podcast_ops", 198, 29,
                        "https://images.unsplash.com/photo-1598488035139-bdbb2231d1bb?w=800&q=80",
                        now - 86400, true, "Paid")
        );
    }

    private static RedditPostDto post(
            String id,
            String title,
            String subreddit,
            String author,
            int score,
            int comments,
            String imageUrl,
            long createdUtc,
            boolean hiring,
            String hiringBadge
    ) {
        String permalink = "https://www.reddit.com/r/" + subreddit + "/comments/" + id + "/";
        double trendingScore = RedditPostRanker.computeTrendingScore(
                score, comments, createdUtc, hiring, title, subreddit, ""
        );
        return new RedditPostDto(
                id,
                title,
                "r/" + subreddit,
                author,
                score,
                comments,
                imageUrl,
                List.of(imageUrl),
                permalink,
                permalink,
                createdUtc,
                RedditApiClient.formatTimeAgo(createdUtc),
                trendingScore,
                hiring,
                hiringBadge,
                "image"
        );
    }
}
