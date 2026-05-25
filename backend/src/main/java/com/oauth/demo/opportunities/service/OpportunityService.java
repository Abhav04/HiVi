package com.oauth.demo.opportunities.service;

import com.oauth.demo.entity.User;
import com.oauth.demo.opportunities.config.OpportunityProperties;
import com.oauth.demo.opportunities.dto.CreateOpportunityRequest;
import com.oauth.demo.opportunities.dto.OpportunityDto;
import com.oauth.demo.opportunities.dto.OpportunityFeedResponse;
import com.oauth.demo.opportunities.entity.*;
import com.oauth.demo.opportunities.repository.OpportunityRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository repository;
    private final OpportunityIngestionService ingestionService;
    private final OpportunityProperties properties;
    private final OpportunityMapper mapper;
    private final CompanyLogoResolver logoResolver;

    public OpportunityService(
            OpportunityRepository repository,
            OpportunityIngestionService ingestionService,
            OpportunityProperties properties,
            OpportunityMapper mapper,
            CompanyLogoResolver logoResolver) {
        this.repository = repository;
        this.ingestionService = ingestionService;
        this.properties = properties;
        this.mapper = mapper;
        this.logoResolver = logoResolver;
    }

    @Cacheable(value = "opportunities-feed", key = "#page + '-' + #size + '-' + #category + '-' + #source")
    public OpportunityFeedResponse getFeed(int page, int size, String category, String source) {
        OpportunityCategory cat = parseCategory(category);
        OpportunitySource src = parseSource(source);
        Pageable pageable = PageRequest.of(page, Math.min(size, 50));

        Page<Opportunity> result = repository.findFiltered(cat, src, pageable);
        List<OpportunityDto> trending = mapper.toDtoList(repository.findTop12ByActiveTrueOrderByTrendingScoreDescPostedAtDesc());
        List<OpportunityDto> latest = mapper.toDtoList(repository.findTop8ByActiveTrueOrderByPostedAtDesc());

        return new OpportunityFeedResponse(
                mapper.toDtoList(result.getContent()),
                trending,
                latest,
                page,
                result.getSize(),
                result.getTotalElements(),
                result.hasNext(),
                category,
                source
        );
    }

    @Cacheable(value = "opportunities-trending")
    public List<OpportunityDto> getTrending() {
        return mapper.toDtoList(repository.findTop12ByActiveTrueOrderByTrendingScoreDescPostedAtDesc());
    }

    @Cacheable(value = "opportunities-feed", key = "'cat-' + #categoryType + '-' + #page")
    public OpportunityFeedResponse getByCategory(String categoryType, int page, int size) {
        return getFeed(page, size, categoryType, null);
    }

    @Transactional
    @CacheEvict(value = {"opportunities-feed", "opportunities-trending"}, allEntries = true)
    public OpportunityDto createUserOpportunity(CreateOpportunityRequest req, User user) {
        validateCreate(req);

        OpportunityCategory category = parseCategory(req.category());
        if (category == null) {
            category = OpportunityClassifier.classifyCategory(req.title(), req.description());
        }
        WorkMode workMode = parseWorkMode(req.workMode());
        if (workMode == null) {
            workMode = OpportunityClassifier.classifyWorkMode(req.title(), req.description());
        }

        List<String> badges = OpportunityClassifier.resolveBadges(req.title(), req.description(), "user");
        List<String> tags = req.tags() != null && !req.tags().isBlank()
                ? List.of(req.tags().split(","))
                : OpportunityClassifier.buildTags(category);

        Opportunity o = new Opportunity();
        o.setSource(OpportunitySource.USER);
        o.setExternalId("user-" + UUID.randomUUID());
        o.setTitle(req.title().trim());
        o.setCompany(req.company() != null ? req.company().trim() : user.getUsername());
        o.setDescription(req.description() != null ? req.description().trim() : "");
        o.setApplyUrl(req.applyUrl().trim());
        o.setPayLabel(req.payLabel());
        o.setWorkMode(workMode);
        o.setCategory(category);
        o.setTags(tags.stream().map(String::trim).collect(Collectors.joining(",")));
        o.setBadges(badges.stream().collect(Collectors.joining(",")));
        o.setPostedBy(user);
        o.setPostedAt(LocalDateTime.now());
        o.setEngagementCount(10);
        OpportunityRanker.refreshTrendingScore(o, 5, 2);
        logoResolver.applyTo(o);

        return mapper.toDto(repository.save(o));
    }

    public void ensureSeeded() {
        if (repository.count() == 0) {
            ingestionService.refreshExternalSources();
        }
    }

    private void validateCreate(CreateOpportunityRequest req) {
        if (req.title() == null || req.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (req.applyUrl() == null || req.applyUrl().isBlank()) {
            throw new IllegalArgumentException("Apply URL is required");
        }
        if (!req.applyUrl().startsWith("http://") && !req.applyUrl().startsWith("https://")) {
            throw new IllegalArgumentException("Apply URL must be http(s)");
        }
    }

    private OpportunityCategory parseCategory(String raw) {
        if (raw == null || raw.isBlank() || "ALL".equalsIgnoreCase(raw)) return null;
        try {
            return OpportunityCategory.valueOf(raw.toUpperCase().replace('-', '_'));
        } catch (Exception e) {
            return null;
        }
    }

    private OpportunitySource parseSource(String raw) {
        if (raw == null || raw.isBlank() || "ALL".equalsIgnoreCase(raw)) return null;
        try {
            return OpportunitySource.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private WorkMode parseWorkMode(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return WorkMode.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
