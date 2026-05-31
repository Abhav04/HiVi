package com.oauth.demo.community.repository;

import com.oauth.demo.community.entity.CommunityPost;
import com.oauth.demo.community.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    @EntityGraph(attributePaths = {"author"})
    Page<CommunityPost> findByStatusOrderByTrendingScoreDescCreatedAtDesc(
            PostStatus status, Pageable pageable);

    Page<CommunityPost> findByStatusAndAuthorIdOrderByCreatedAtDesc(
            PostStatus status, Long authorId, Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    @Query("""
            SELECT p FROM CommunityPost p
            WHERE p.status = :status
            AND p.author.id IN (
                SELECT f.following.id FROM CreatorFollow f WHERE f.follower.id = :userId
            )
            ORDER BY p.trendingScore DESC, p.createdAt DESC
            """)
    Page<CommunityPost> findFollowingFeed(
            @Param("userId") Long userId,
            @Param("status") PostStatus status,
            Pageable pageable);

    @EntityGraph(attributePaths = {"author"})
    List<CommunityPost> findTop10ByStatusOrderByLikeCountDescCreatedAtDesc(PostStatus status);

    List<CommunityPost> findTop6ByStatusAndPostTypeOrderByTrendingScoreDesc(
            PostStatus status, com.oauth.demo.community.entity.PostType postType);

    Page<CommunityPost> findByAuthorIdAndStatusOrderByUpdatedAtDesc(
            Long authorId, PostStatus status, Pageable pageable);

    Page<CommunityPost> findByAuthorIdOrderByUpdatedAtDesc(Long authorId, Pageable pageable);

    java.util.Optional<CommunityPost> findByIdAndAuthorId(Long id, Long authorId);
}
