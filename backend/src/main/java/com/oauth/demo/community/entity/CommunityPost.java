package com.oauth.demo.community.entity;

import com.oauth.demo.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "community_posts", indexes = {
        @Index(name = "idx_community_posts_created", columnList = "createdAt"),
        @Index(name = "idx_community_posts_author", columnList = "author_id"),
        @Index(name = "idx_community_posts_status", columnList = "status")
})
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 4000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType = PostType.TEXT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.PUBLISHED;

    private String mediaUrl;
    private String thumbnailUrl;
    private String portfolioLink;

    /** Comma-separated edit styles: cinematic, anime, gaming, etc. */
    @Column(length = 500)
    private String tags;

    private int likeCount = 0;
    private int commentCount = 0;
    private int viewCount = 0;
    private int bookmarkCount = 0;

    private double trendingScore = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CommunityPost() {}

    public Long getId() { return id; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public PostType getPostType() { return postType; }
    public void setPostType(PostType postType) { this.postType = postType; }
    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public String getPortfolioLink() { return portfolioLink; }
    public void setPortfolioLink(String portfolioLink) { this.portfolioLink = portfolioLink; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public int getBookmarkCount() { return bookmarkCount; }
    public void setBookmarkCount(int bookmarkCount) { this.bookmarkCount = bookmarkCount; }
    public double getTrendingScore() { return trendingScore; }
    public void setTrendingScore(double trendingScore) { this.trendingScore = trendingScore; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
