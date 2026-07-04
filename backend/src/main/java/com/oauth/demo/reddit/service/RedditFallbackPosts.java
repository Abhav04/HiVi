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
                post("fb1", "r/VideoEditor_forhire: Video editing jobs & portfolios",
                        "VideoEditor_forhire", "moderators", 142, 38,
                        "https://images.unsplash.com/photo-1598488035139-bdbb2231d1bb?w=800&q=80",
                        now - 7200, true, "Hiring",
                        "/r/VideoEditor_forhire/"),
                post("fb2", "Want to learn video editing? Start here!",
                        "videoediting", "mods", 891, 124,
                        "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80",
                        now - 14400, false, null,
                        "/r/videoediting/comments/14y9t3c/want_to_learn_video_editing_start_here/"),
                post("fb3", "Meta: How to hire and be hired on r/forhire",
                        "forhire", "forhire_mod", 256, 41,
                        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                        now - 21600, true, "Paid",
                        "/r/forhire/comments/8w1b8a/meta_how_to_hire_and_be_hired_on_rforhire/"),
                post("fb4", "r/VideoEditors FAQ: Read this first!",
                        "VideoEditors", "editor_mod", 1204, 89,
                        "https://images.unsplash.com/photo-1612036789805-4b12b83956c4?w=800&q=80",
                        now - 28800, false, null,
                        "/r/VideoEditors/comments/w43g46/rvideoeditors_faq_read_this_first/"),
                post("fb5", "FreelanceIndia Frequently Asked Questions (FAQ)",
                        "FreelanceIndia", "india_freelance", 178, 52,
                        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&q=80",
                        now - 36000, true, "Available",
                        "/r/FreelanceIndia/comments/tbiz5o/freelanceindia_frequently_asked_questions_faq/"),
                post("fb6", "Welcome to r/Premiere! Please read before posting.",
                        "premiere", "premiere_mod", 445, 167,
                        "https://images.unsplash.com/photo-1611532736597-de2d90e850f7?w=800&q=80",
                        now - 43200, false, null,
                        "/r/premiere/comments/119a0a3/welcome_to_rpremiere_please_read_before_posting/"),
                post("fb7", "r/Filmmakers FAQ: Read this first!",
                        "Filmmakers", "filmmaker_mod", 623, 71,
                        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=800&q=80",
                        now - 50400, false, null,
                        "/r/Filmmakers/comments/7g72v7/rfilmmakers_faq_read_this_first/"),
                post("fb8", "Introducing r/AfterEffects FAQ — read this before posting!",
                        "AfterEffects", "ae_mod", 512, 93,
                        "https://images.unsplash.com/photo-1478720568477-152d9b8e6839?w=800&q=80",
                        now - 57600, false, null,
                        "/r/AfterEffects/comments/12zpq4y/introducing_raftereffects_faq_read_this_before/"),
                post("fb9", "r/VideoEditor_forhire: Video editing jobs board",
                        "VideoEditor_forhire", "moderators", 334, 45,
                        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80",
                        now - 64800, true, "Hiring",
                        "/r/VideoEditor_forhire/"),
                post("fb10", "Want to learn video editing? Start here!",
                        "videoediting", "mods", 389, 31,
                        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800&q=80",
                        now - 72000, false, null,
                        "/r/videoediting/comments/14y9t3c/want_to_learn_video_editing_start_here/"),
                post("fb11", "r/videography FAQ: Read this first!",
                        "videography", "videography_mod", 267, 88,
                        "https://images.unsplash.com/photo-1519741497674-611481863552?w=800&q=80",
                        now - 79200, false, null,
                        "/r/videography/comments/10cwtpx/rvideography_faq_read_this_first/"),
                post("fb12", "Meta: How to hire and be hired on r/forhire",
                        "forhire", "forhire_mod", 198, 29,
                        "https://images.unsplash.com/photo-1598488035139-bdbb2231d1bb?w=800&q=80",
                        now - 86400, true, "Paid",
                        "/r/forhire/comments/8w1b8a/meta_how_to_hire_and_be_hired_on_rforhire/")
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
            String hiringBadge,
            String permalinkPath
    ) {
        String permalink = permalinkPath.startsWith("http") ? permalinkPath : "https://www.reddit.com" + permalinkPath;
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
