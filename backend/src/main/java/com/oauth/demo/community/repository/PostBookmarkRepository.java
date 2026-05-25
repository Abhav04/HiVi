package com.oauth.demo.community.repository;

import com.oauth.demo.community.entity.PostBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostBookmarkRepository extends JpaRepository<PostBookmark, Long> {

    Optional<PostBookmark> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);
}
