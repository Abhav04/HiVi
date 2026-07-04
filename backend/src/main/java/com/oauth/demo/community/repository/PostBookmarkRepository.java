package com.oauth.demo.community.repository;

import com.oauth.demo.community.entity.CommunityPost;
import com.oauth.demo.community.entity.PostBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    Optional<PostBookmark> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Query("""
            SELECT b.post FROM PostBookmark b
            WHERE b.user.id = :userId AND b.post.status = com.oauth.demo.community.entity.PostStatus.PUBLISHED
            ORDER BY b.createdAt DESC
            """)
    Page<CommunityPost> findBookmarkedPosts(@Param("userId") Long userId, Pageable pageable);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM PostBookmark b WHERE b.post.id = :postId")
    void deleteByPostId(@org.springframework.data.repository.query.Param("postId") Long postId);
}
