package com.oauth.demo.opportunities.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.demo.opportunities.entity.Opportunity;
import com.oauth.demo.opportunities.entity.OpportunitySource;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CompanyLogoResolver {

    private static final Set<String> GENERIC_DOMAINS = Set.of(
            "reddit.com", "www.reddit.com", "old.reddit.com",
            "linkedin.com", "www.linkedin.com",
            "internshala.com", "www.internshala.com",
            "google.com", "forms.ggle.com"
    );

    private static final Pattern DOMAIN_IN_TEXT = Pattern.compile(
            "(?:https?://)?(?:www\\.)?([a-z0-9][-a-z0-9]*\\.)+[a-z]{2,}",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern CLIENT_FROM_TITLE = Pattern.compile(
            "^\\s*(?:\\[[^\\]]+\\]\\s*)*(.+?)\\s+(?:is\\s+)?(?:looking for|hiring|needs|seeking)\\b",
            Pattern.CASE_INSENSITIVE);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, String> companyDomains = new HashMap<>();
    private Map<OpportunitySource, String> sourceDomains = new HashMap<>();

    @PostConstruct
    void loadMappings() throws Exception {
        JsonNode root = objectMapper.readTree(new ClassPathResource("company-logo-domains.json").getInputStream());
        root.path("byCompanyName").fields().forEachRemaining(e ->
                companyDomains.put(normalizeKey(e.getKey()), e.getValue().asText()));
        root.path("bySource").fields().forEachRemaining(e -> {
            try {
                sourceDomains.put(OpportunitySource.valueOf(e.getKey()), e.getValue().asText());
            } catch (IllegalArgumentException ignored) {
                // skip unknown keys
            }
        });
    }

    public record LogoResolution(String logoUrl, String logoFallbackUrl, String companyInitials) {}

    public LogoResolution resolve(Opportunity o) {
        return resolve(o.getSource(), o.getCompany(), o.getApplyUrl(), o.getTitle(), o.getLogoUrl());
    }

    public LogoResolution resolve(
            OpportunitySource source,
            String company,
            String applyUrl,
            String title,
            String storedLogoUrl) {

        if (storedLogoUrl != null && !storedLogoUrl.isBlank()) {
            String domain = extractHost(applyUrl);
            return build(storedLogoUrl, domain, company, source);
        }

        String domain = resolveDomain(source, company, applyUrl, title);
        if (domain == null) {
            return new LogoResolution(null, null, initials(company, source));
        }

        String clearbit = "https://logo.clearbit.com/" + domain;
        String favicon = "https://www.google.com/s2/favicons?domain=" + domain + "&sz=128";
        return build(clearbit, domain, company, source, favicon);
    }

    public String resolveAndStoreUrl(Opportunity o) {
        LogoResolution r = resolve(o.getSource(), o.getCompany(), o.getApplyUrl(), o.getTitle(), null);
        o.setLogoUrl(r.logoUrl());
        o.setLogoFallbackUrl(r.logoFallbackUrl());
        return r.logoUrl();
    }

    public void applyTo(Opportunity o) {
        LogoResolution r = resolve(o);
        if (o.getLogoUrl() == null || o.getLogoUrl().isBlank()) {
            o.setLogoUrl(r.logoUrl());
        }
        if (o.getLogoFallbackUrl() == null || o.getLogoFallbackUrl().isBlank()) {
            o.setLogoFallbackUrl(r.logoFallbackUrl());
        }
    }

    public static String parseClientCompany(String title, String subreddit) {
        if (title == null || title.isBlank()) {
            return "r/" + subreddit;
        }
        Matcher m = CLIENT_FROM_TITLE.matcher(title.trim());
        if (m.find()) {
            String candidate = m.group(1).trim();
            if (candidate.length() >= 3 && candidate.length() <= 60) {
                return candidate;
            }
        }
        Matcher domainMatch = DOMAIN_IN_TEXT.matcher(title);
        if (domainMatch.find()) {
            String host = domainMatch.group().toLowerCase(Locale.ROOT).replaceFirst("^https?://", "").replace("www.", "");
            int slash = host.indexOf('/');
            if (slash > 0) host = host.substring(0, slash);
            if (!GENERIC_DOMAINS.contains(host)) {
                String name = host.contains(".") ? host.substring(0, host.indexOf('.')) : host;
                return capitalize(name);
            }
        }
        return "r/" + subreddit;
    }

    private String resolveDomain(OpportunitySource source, String company, String applyUrl, String title) {
        if (company != null) {
            String mapped = companyDomains.get(normalizeKey(company));
            if (mapped != null) return mapped;
        }

        String fromUrl = extractHost(applyUrl);
        if (fromUrl != null && !GENERIC_DOMAINS.contains(fromUrl)) {
            return fromUrl;
        }

        if (title != null) {
            Matcher m = DOMAIN_IN_TEXT.matcher(title);
            while (m.find()) {
                String raw = m.group().toLowerCase(Locale.ROOT).replaceFirst("^https?://", "").replace("www.", "");
                int slash = raw.indexOf('/');
                if (slash > 0) raw = raw.substring(0, slash);
                if (!GENERIC_DOMAINS.contains(raw)) {
                    return raw;
                }
            }
        }

        if (source != null && sourceDomains.containsKey(source)) {
            return sourceDomains.get(source);
        }

        return fromUrl;
    }

    private LogoResolution build(String logoUrl, String domain, String company, OpportunitySource source) {
        String favicon = domain != null
                ? "https://www.google.com/s2/favicons?domain=" + domain + "&sz=128"
                : null;
        return build(logoUrl, domain, company, source, favicon);
    }

    private LogoResolution build(
            String logoUrl,
            String domain,
            String company,
            OpportunitySource source,
            String favicon) {

        return new LogoResolution(logoUrl, favicon, initials(company, source));
    }

    private String initials(String company, OpportunitySource source) {
        if (company != null && !company.isBlank()) {
            String c = company.trim();
            if (c.startsWith("r/") && c.length() > 2) {
                return c.substring(2, Math.min(4, c.length())).toUpperCase(Locale.ROOT);
            }
            String[] parts = c.split("\\s+");
            if (parts.length >= 2) {
                return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase(Locale.ROOT);
            }
            return c.substring(0, Math.min(2, c.length())).toUpperCase(Locale.ROOT);
        }
        if (source != null) {
            return switch (source) {
                case REDDIT -> "RD";
                case LINKEDIN -> "IN";
                case INTERNSHALA -> "IS";
                case USER -> "HV";
            };
        }
        return "??";
    }

    private static String extractHost(String url) {
        if (url == null || url.isBlank()) return null;
        try {
            URI uri = URI.create(url.trim());
            String host = uri.getHost();
            if (host == null) return null;
            return host.toLowerCase(Locale.ROOT).replaceFirst("^www\\.", "");
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeKey(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
