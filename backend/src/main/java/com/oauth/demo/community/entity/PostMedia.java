package com.oauth.demo.community.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_media", indexes = {
        @Index(name = "idx_post_media_post", columnList = "post_id")
})
public class PostMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @Column(nullable = false, length = 512)
    private String mediaUrl;

    @Column(length = 512)
    private String thumbnailUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private MediaKind mediaKind = MediaKind.IMAGE;

    private int sortOrder = 0;

    public enum MediaKind {
        IMAGE,
        VIDEO
    }

    public Long getId() { return id; }
    public CommunityPost getPost() { return post; }
    public void setPost(CommunityPost post) { this.post = post; }
    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
    public MediaKind getMediaKind() { return mediaKind; }
    public void setMediaKind(MediaKind mediaKind) { this.mediaKind = mediaKind; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
