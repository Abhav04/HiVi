package com.oauth.demo.opportunities.service;

import com.oauth.demo.opportunities.entity.Opportunity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

public final class OpportunityRanker {

    private OpportunityRanker() {}

    public static double computeScore(
            int upvotes,
            int comments,
            LocalDateTime postedAt,
            List<String> badges) {

        double score = 0;
        score += Math.min(upvotes, 500) * 0.15;
        score += Math.min(comments, 200) * 0.4;

        if (postedAt != null) {
            long hours = ChronoUnit.HOURS.between(postedAt, LocalDateTime.now());
            score += Math.max(0, 72 - hours) * 0.35;
        }

        if (badges != null) {
            if (badges.contains("Urgent")) score += 18;
            if (badges.contains("Paid")) score += 12;
            if (badges.contains("Remote")) score += 8;
            if (badges.contains("Internship")) score += 6;
        }

        return score;
    }

    /** Overload for entity after badges stored */
    public static void refreshTrendingScore(Opportunity o, int upvotes, int comments) {
        List<String> badges = o.getBadges() == null ? List.of()
                : List.of(o.getBadges().split(","));
        o.setTrendingScore(computeScore(upvotes, comments, o.getPostedAt(), badges));
        o.setEngagementCount(upvotes + comments);
    }

    public static Comparator<Opportunity> trendingComparator() {
        return Comparator
                .comparingDouble(Opportunity::getTrendingScore).reversed()
                .thenComparing(Opportunity::getPostedAt, Comparator.nullsLast(Comparator.reverseOrder()));
    }
}
