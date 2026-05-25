package com.oauth.demo.opportunities.repository;

import com.oauth.demo.opportunities.entity.Opportunity;
import com.oauth.demo.opportunities.entity.OpportunityCategory;
import com.oauth.demo.opportunities.entity.OpportunitySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    Optional<Opportunity> findBySourceAndExternalId(OpportunitySource source, String externalId);

    Page<Opportunity> findByActiveTrueOrderByPostedAtDesc(Pageable pageable);

    Page<Opportunity> findByActiveTrueAndCategoryOrderByTrendingScoreDesc(
            OpportunityCategory category, Pageable pageable);

    Page<Opportunity> findByActiveTrueAndSourceOrderByPostedAtDesc(
            OpportunitySource source, Pageable pageable);

    @Query("""
            SELECT o FROM Opportunity o WHERE o.active = true
            AND (:category IS NULL OR o.category = :category)
            AND (:source IS NULL OR o.source = :source)
            ORDER BY o.trendingScore DESC, o.postedAt DESC
            """)
    Page<Opportunity> findFiltered(
            @Param("category") OpportunityCategory category,
            @Param("source") OpportunitySource source,
            Pageable pageable);

    List<Opportunity> findTop12ByActiveTrueOrderByTrendingScoreDescPostedAtDesc();

    List<Opportunity> findTop8ByActiveTrueOrderByPostedAtDesc();
}
