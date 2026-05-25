package com.oauth.demo.opportunities.fetch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.demo.opportunities.entity.*;
import com.oauth.demo.opportunities.service.CompanyLogoResolver;
import com.oauth.demo.opportunities.service.OpportunityClassifier;
import com.oauth.demo.opportunities.service.OpportunityRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads hand-curated discovery links (Internshala hubs, LinkedIn search URLs).
 * No HTML scraping — only static metadata we maintain.
 */
@Component
public class CuratedOpportunityProvider {

    private static final Logger log = LoggerFactory.getLogger(CuratedOpportunityProvider.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CompanyLogoResolver logoResolver;

    public CuratedOpportunityProvider(CompanyLogoResolver logoResolver) {
        this.logoResolver = logoResolver;
    }

    public List<Opportunity> loadCurated() {
        try (InputStream in = new ClassPathResource("curated-opportunities.json").getInputStream()) {
            JsonNode root = objectMapper.readTree(in);
            List<Opportunity> list = new ArrayList<>();
            for (JsonNode node : root) {
                list.add(mapNode(node));
            }
            return list;
        } catch (Exception ex) {
            log.warn("Could not load curated opportunities: {}", ex.getMessage());
            return List.of();
        }
    }

    private Opportunity mapNode(JsonNode node) {
        String title = node.path("title").asText();
        String body = node.path("description").asText();
        OpportunityCategory category = parseCategory(node.path("category").asText());
        WorkMode workMode = parseWorkMode(node.path("workMode").asText());

        List<String> badges = OpportunityClassifier.resolveBadges(title, body, "curated");
        List<String> tags = new ArrayList<>(OpportunityClassifier.buildTags(category));
        if (node.has("tags")) {
            for (String t : node.path("tags").asText().split(",")) {
                if (!t.isBlank() && !tags.contains(t.trim())) {
                    tags.add(t.trim());
                }
            }
        }

        Opportunity o = new Opportunity();
        o.setSource(OpportunitySource.valueOf(node.path("source").asText("INTERNSHALA")));
        o.setExternalId(node.path("externalId").asText());
        o.setTitle(title);
        o.setCompany(node.path("company").asText("Partner"));
        o.setDescription(body);
        o.setApplyUrl(node.path("applyUrl").asText());
        o.setPayLabel(node.path("payLabel").asText(null));
        o.setWorkMode(workMode);
        o.setCategory(category);
        o.setTags(String.join(",", tags));
        o.setBadges(badges.stream().map(String::trim).collect(Collectors.joining(",")));
        o.setPostedAt(LocalDateTime.now().minusDays(1));
        o.setEngagementCount(50);
        OpportunityRanker.refreshTrendingScore(o, 30, 5);
        o.setActive(true);
        if (node.has("logoUrl") && !node.path("logoUrl").asText("").isBlank()) {
            o.setLogoUrl(node.path("logoUrl").asText());
        }
        logoResolver.applyTo(o);
        return o;
    }

    private OpportunityCategory parseCategory(String raw) {
        try {
            return OpportunityCategory.valueOf(raw);
        } catch (Exception e) {
            return OpportunityClassifier.classifyCategory("", raw);
        }
    }

    private WorkMode parseWorkMode(String raw) {
        try {
            return WorkMode.valueOf(raw);
        } catch (Exception e) {
            return WorkMode.UNKNOWN;
        }
    }
}
