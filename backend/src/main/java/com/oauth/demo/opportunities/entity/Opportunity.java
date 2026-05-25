package com.oauth.demo.opportunities.entity;

import com.oauth.demo.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities", indexes = {
        @Index(name = "idx_opp_posted", columnList = "postedAt"),
        @Index(name = "idx_opp_trending", columnList = "trendingScore"),
        @Index(name = "idx_opp_category", columnList = "category"),
        @Index(name = "idx_opp_source_ext", columnList = "source,externalId", unique = true)
})
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OpportunitySource source;

    @Column(length = 128)
    private String externalId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 200)
    private String company;

    @Column(length = 512)
    private String logoUrl;

    @Column(length = 512)
    private String logoFallbackUrl;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 1000)
    private String applyUrl;

    @Column(length = 120)
    private String payLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkMode workMode = WorkMode.UNKNOWN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OpportunityCategory category = OpportunityCategory.GENERAL_EDITING;

    @Column(length = 500)
    private String tags;

    @Column(length = 200)
    private String badges;

    private boolean active = true;
    private double trendingScore = 0;
    private int engagementCount = 0;

    private LocalDateTime postedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posted_by_user_id")
    private User postedBy;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
        if (postedAt == null) postedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OpportunitySource getSource() { return source; }
    public void setSource(OpportunitySource source) { this.source = source; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getLogoFallbackUrl() { return logoFallbackUrl; }
    public void setLogoFallbackUrl(String logoFallbackUrl) { this.logoFallbackUrl = logoFallbackUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getApplyUrl() { return applyUrl; }
    public void setApplyUrl(String applyUrl) { this.applyUrl = applyUrl; }
    public String getPayLabel() { return payLabel; }
    public void setPayLabel(String payLabel) { this.payLabel = payLabel; }
    public WorkMode getWorkMode() { return workMode; }
    public void setWorkMode(WorkMode workMode) { this.workMode = workMode; }
    public OpportunityCategory getCategory() { return category; }
    public void setCategory(OpportunityCategory category) { this.category = category; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getBadges() { return badges; }
    public void setBadges(String badges) { this.badges = badges; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public double getTrendingScore() { return trendingScore; }
    public void setTrendingScore(double trendingScore) { this.trendingScore = trendingScore; }
    public int getEngagementCount() { return engagementCount; }
    public void setEngagementCount(int engagementCount) { this.engagementCount = engagementCount; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public User getPostedBy() { return postedBy; }
    public void setPostedBy(User postedBy) { this.postedBy = postedBy; }
}
