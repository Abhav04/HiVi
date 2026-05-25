package com.oauth.demo.community.repository;

import com.oauth.demo.community.entity.CreatorFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreatorFollowRepository extends JpaRepository<CreatorFollow, Long> {

    Optional<CreatorFollow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    long countByFollowingId(Long followingId);

    long countByFollowerId(Long followerId);
}
