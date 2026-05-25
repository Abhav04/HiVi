package com.oauth.demo.community.entity;

import com.oauth.demo.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments", indexes = {
        @Index(name = "idx_post_comments_post", columnList = "post_id")
})
public class PostComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private PostComment parent;

    @Column(nullable = false, length = 2000)
    private String content;

    private int likeCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PostComment() {}

    public Long getId() { return id; }
    public CommunityPost getPost() { return post; }
    public void setPost(CommunityPost post) { this.post = post; }
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
    public PostComment getParent() { return parent; }
    public void setParent(PostComment parent) { this.parent = parent; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
