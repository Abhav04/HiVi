package com.oauth.demo.opportunities.service;

import com.oauth.demo.opportunities.dto.OpportunityDto;
import com.oauth.demo.opportunities.entity.Opportunity;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OpportunityMapper {

    private final CompanyLogoResolver logoResolver;

    public OpportunityMapper(CompanyLogoResolver logoResolver) {
        this.logoResolver = logoResolver;
    }

    public OpportunityDto toDto(Opportunity o) {
        CompanyLogoResolver.LogoResolution logo = logoResolver.resolve(o);
        return new OpportunityDto(
                o.getId(),
                o.getSource().name(),
                o.getTitle(),
                o.getCompany(),
                logo.logoUrl(),
                logo.logoFallbackUrl(),
                logo.companyInitials(),
                o.getDescription(),
                o.getApplyUrl(),
                o.getPayLabel(),
                o.getWorkMode().name(),
                o.getCategory().name(),
                parseList(o.getTags()),
                parseList(o.getBadges()),
                o.getTrendingScore(),
                o.getEngagementCount(),
                o.getPostedAt(),
                formatTimeAgo(o)
        );
    }

    public List<OpportunityDto> toDtoList(List<Opportunity> list) {
        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    private static List<String> parseList(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static String formatTimeAgo(Opportunity o) {
        if (o.getPostedAt() == null) return "recently";
        long epoch = o.getPostedAt().toEpochSecond(ZoneOffset.UTC);
        if (epoch <= 0) return "recently";
        Duration ago = Duration.between(Instant.ofEpochSecond(epoch), Instant.now());
        long hours = ago.toHours();
        if (hours < 1) {
            long mins = Math.max(ago.toMinutes(), 1);
            return mins + "m ago";
        }
        if (hours < 48) return hours + "h ago";
        return ago.toDays() + "d ago";
    }
}
