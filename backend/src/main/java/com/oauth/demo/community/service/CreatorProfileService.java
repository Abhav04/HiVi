package com.oauth.demo.community.service;

import com.oauth.demo.community.dto.CreatorProfileDto;
import com.oauth.demo.community.entity.CreatorProfile;
import com.oauth.demo.community.repository.CreatorProfileRepository;
import com.oauth.demo.entity.User;
import com.oauth.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreatorProfileService {

    private final CreatorProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final CommunityMapper mapper;
    private final com.oauth.demo.community.repository.CommunityPostRepository postRepository;

    public CreatorProfileService(
            CreatorProfileRepository profileRepository,
            UserRepository userRepository,
            CommunityMapper mapper,
            com.oauth.demo.community.repository.CommunityPostRepository postRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
        this.postRepository = postRepository;
    }

    @Transactional
    public CreatorProfile getOrCreate(User user) {
        return profileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    CreatorProfile profile = new CreatorProfile();
                    profile.setUser(user);
                    profile.setCreatedAt(LocalDateTime.now());
                    profile.setUpdatedAt(LocalDateTime.now());
                    return profileRepository.save(profile);
                });
    }

    @Transactional(readOnly = true)
    public CreatorProfileDto getProfile(String username, User viewer) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Creator not found");
        }
        CreatorProfile profile = getOrCreate(user);
        var recentPage = postRepository.findByStatusAndAuthorIdOrderByCreatedAtDesc(
                com.oauth.demo.community.entity.PostStatus.PUBLISHED,
                user.getId(),
                org.springframework.data.domain.PageRequest.of(0, 6));
        var recent = recentPage.getContent().stream()
                .map(p -> mapper.toPostDto(p, viewer))
                .toList();
        return mapper.toProfileDto(profile, user, viewer, recent);
    }

    @Transactional(readOnly = true)
    public List<com.oauth.demo.community.dto.AuthorSummaryDto> getTrendingCreators(User viewer) {
        return profileRepository.findTop12ByOrderByTotalLikesDescTotalPostsDesc().stream()
                .map(p -> mapper.toAuthorSummary(p.getUser(), viewer))
                .toList();
    }

    @Transactional
    public CreatorProfileDto updateMyProfile(User user, CreatorProfileUpdate update) {
        CreatorProfile profile = getOrCreate(user);
        if (update.bio() != null) profile.setBio(update.bio());
        if (update.niche() != null) profile.setNiche(update.niche());
        if (update.tools() != null) profile.setTools(update.tools());
        if (update.portfolioUrl() != null) profile.setPortfolioUrl(update.portfolioUrl());
        if (update.availableForWork() != null) profile.setAvailableForWork(update.availableForWork());
        profile.setUpdatedAt(LocalDateTime.now());
        profileRepository.save(profile);
        return mapper.toProfileDto(profile, user, user, List.of());
    }

    public record CreatorProfileUpdate(
            String bio,
            String niche,
            String tools,
            String portfolioUrl,
            Boolean availableForWork
    ) {}
}
