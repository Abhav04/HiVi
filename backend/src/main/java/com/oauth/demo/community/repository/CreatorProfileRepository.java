package com.oauth.demo.community.repository;

import com.oauth.demo.community.entity.CreatorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreatorProfileRepository extends JpaRepository<CreatorProfile, Long> {

    Optional<CreatorProfile> findByUserId(Long userId);

    Optional<CreatorProfile> findByUserUsername(String username);

    List<CreatorProfile> findTop12ByOrderByTotalLikesDescTotalPostsDesc();
}
