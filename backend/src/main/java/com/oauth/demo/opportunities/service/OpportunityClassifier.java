package com.oauth.demo.opportunities.service;

import com.oauth.demo.opportunities.entity.OpportunityCategory;
import com.oauth.demo.opportunities.entity.WorkMode;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps free-text job posts to editor-focused categories, tags, and badges.
 */
public final class OpportunityClassifier {

    private static final Pattern PAY_PATTERN = Pattern.compile(
            "(₹|\\$|rs\\.?|inr|usd|stipend|salary|budget|/hr|per\\s+video|paid)",
            Pattern.CASE_INSENSITIVE);

    private OpportunityClassifier() {}

    public static OpportunityCategory classifyCategory(String title, String body) {
        String text = combined(title, body);
        if (containsAny(text, "reel", "instagram reel", "short form reel")) return OpportunityCategory.REEL_EDITING;
        if (containsAny(text, "anime", "amv")) return OpportunityCategory.ANIME_EDITING;
        if (containsAny(text, "gaming", "game edit", "montage", "valorant", "minecraft")) return OpportunityCategory.GAMING_EDITS;
        if (containsAny(text, "youtube", "longform", "long-form", "podcast edit")) return OpportunityCategory.YOUTUBE_LONGFORM;
        if (containsAny(text, "motion graphic", "after effects", "aftereffects", "mograph")) return OpportunityCategory.MOTION_GRAPHICS;
        if (containsAny(text, "shorts", "tiktok", "short-form")) return OpportunityCategory.SHORTS_EDITING;
        if (containsAny(text, "intern", "internship", "trainee")) return OpportunityCategory.INTERNSHIP;
        if (containsAny(text, "freelance", "contract", "gig", "client project")) return OpportunityCategory.FREELANCE;
        if (containsAny(text, "remote", "work from home", "wfh")) return OpportunityCategory.REMOTE_WORK;
        return OpportunityCategory.GENERAL_EDITING;
    }

    public static WorkMode classifyWorkMode(String title, String body) {
        String text = combined(title, body);
        if (containsAny(text, "remote", "work from home", "wfh", "anywhere")) return WorkMode.REMOTE;
        if (containsAny(text, "on-site", "onsite", "in-office", "in office", "hybrid")) {
            if (text.contains("hybrid")) return WorkMode.HYBRID;
            return WorkMode.ON_SITE;
        }
        return WorkMode.UNKNOWN;
    }

    public static List<String> resolveBadges(String title, String body, String subredditOrSource) {
        Set<String> badges = new LinkedHashSet<>();
        String text = combined(title, body);

        badges.add("Hiring");
        if (PAY_PATTERN.matcher(text).find()) badges.add("Paid");
        if (containsAny(text, "urgent", "asap", "immediate", "rush")) badges.add("Urgent");
        if (containsAny(text, "remote", "wfh", "work from home")) badges.add("Remote");
        if (containsAny(text, "intern", "internship", "stipend")) badges.add("Internship");
        if (containsAny(text, "freelance", "contract", "gig") || "freelance".equalsIgnoreCase(subredditOrSource)) {
            badges.add("Freelance");
        }

        return new ArrayList<>(badges);
    }

    public static String extractPayLabel(String title, String body) {
        String text = combined(title, body);
        Matcher m = Pattern.compile("(₹|rs\\.?|inr|\\$)\\s?[\\d,.]+[kK]?(\\s?/\\s?(month|mo|hr|video|project))?", Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) return m.group().trim();
        if (text.contains("unpaid")) return "Unpaid / portfolio";
        if (PAY_PATTERN.matcher(text).find()) return "Paid — see listing";
        return null;
    }

    public static List<String> buildTags(OpportunityCategory category) {
        return switch (category) {
            case REEL_EDITING -> List.of("reels", "social", "short-form");
            case GAMING_EDITS -> List.of("gaming", "montage", "esports");
            case ANIME_EDITING -> List.of("anime", "amv");
            case YOUTUBE_LONGFORM -> List.of("youtube", "longform");
            case MOTION_GRAPHICS -> List.of("motion", "after-effects");
            case SHORTS_EDITING -> List.of("shorts", "tiktok");
            case FREELANCE -> List.of("freelance", "client-work");
            case INTERNSHIP -> List.of("internship", "learning");
            case REMOTE_WORK -> List.of("remote", "editor");
            default -> List.of("video-editing", "creator");
        };
    }

    private static String combined(String title, String body) {
        return ((title == null ? "" : title) + " " + (body == null ? "" : body)).toLowerCase(Locale.ROOT);
    }

    private static boolean containsAny(String text, String... needles) {
        for (String n : needles) {
            if (text.contains(n)) return true;
        }
        return false;
    }
}
