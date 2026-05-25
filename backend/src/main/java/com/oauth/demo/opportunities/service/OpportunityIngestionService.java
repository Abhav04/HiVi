package com.oauth.demo.opportunities.service;

import com.oauth.demo.opportunities.entity.Opportunity;
import com.oauth.demo.opportunities.entity.OpportunitySource;
import com.oauth.demo.opportunities.fetch.CuratedOpportunityProvider;
import com.oauth.demo.opportunities.fetch.RedditOpportunityFetcher;
import com.oauth.demo.opportunities.repository.OpportunityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OpportunityIngestionService {

    private static final Logger log = LoggerFactory.getLogger(OpportunityIngestionService.class);

    private final RedditOpportunityFetcher redditFetcher;
    private final CuratedOpportunityProvider curatedProvider;
    private final OpportunityRepository repository;
    private final CompanyLogoResolver logoResolver;

    public OpportunityIngestionService(
            RedditOpportunityFetcher redditFetcher,
            CuratedOpportunityProvider curatedProvider,
            OpportunityRepository repository,
            CompanyLogoResolver logoResolver) {
        this.redditFetcher = redditFetcher;
        this.curatedProvider = curatedProvider;
        this.repository = repository;
        this.logoResolver = logoResolver;
    }

    @Transactional
    @CacheEvict(value = {"opportunities-feed", "opportunities-trending"}, allEntries = true)
    public int refreshExternalSources() {
        int upserted = 0;
        upserted += upsertAll(redditFetcher.fetchHiringPosts());
        upserted += upsertAll(curatedProvider.loadCurated());
        log.info("Opportunity refresh complete — {} records upserted", upserted);
        return upserted;
    }

    private int upsertAll(List<Opportunity> incoming) {
        int count = 0;
        for (Opportunity draft : incoming) {
            if (draft.getApplyUrl() == null || draft.getApplyUrl().isBlank()) {
                continue;
            }
            Opportunity existing = repository
                    .findBySourceAndExternalId(draft.getSource(), draft.getExternalId())
                    .orElse(null);

            logoResolver.applyTo(draft);

            if (existing == null) {
                repository.save(draft);
                count++;
            } else if (existing.getSource() != OpportunitySource.USER) {
                mergeExternal(existing, draft);
                repository.save(existing);
                count++;
            }
        }
        return count;
    }

    private void mergeExternal(Opportunity existing, Opportunity draft) {
        existing.setTitle(draft.getTitle());
        existing.setCompany(draft.getCompany());
        existing.setDescription(draft.getDescription());
        existing.setApplyUrl(draft.getApplyUrl());
        existing.setPayLabel(draft.getPayLabel());
        existing.setWorkMode(draft.getWorkMode());
        existing.setCategory(draft.getCategory());
        existing.setTags(draft.getTags());
        existing.setBadges(draft.getBadges());
        existing.setPostedAt(draft.getPostedAt());
        existing.setTrendingScore(draft.getTrendingScore());
        existing.setEngagementCount(draft.getEngagementCount());
        existing.setLogoUrl(draft.getLogoUrl());
        existing.setLogoFallbackUrl(draft.getLogoFallbackUrl());
        existing.setActive(true);
    }
}
