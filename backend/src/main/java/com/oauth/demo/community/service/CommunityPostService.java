package com.oauth.demo.community.service;

import com.oauth.demo.community.dto.CommunityFeedResponse;
import com.oauth.demo.community.dto.CommunityPostDto;
import com.oauth.demo.community.entity.CommunityPost;
import com.oauth.demo.community.entity.CreatorProfile;
import com.oauth.demo.community.entity.PostStatus;
import com.oauth.demo.community.entity.PostType;
import com.oauth.demo.community.repository.CommunityPostRepository;
import com.oauth.demo.community.repository.CreatorProfileRepository;
import com.oauth.demo.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final CreatorProfileRepository profileRepository;
    private final CommunityMapper mapper;
    private final CommunityMediaStorageService mediaStorage;
    public CommunityPostService(
            CommunityPostRepository postRepository,
            CreatorProfileRepository profileRepository,
            CommunityMapper mapper,
            CommunityMediaStorageService mediaStorage) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
        this.mapper = mapper;
        this.mediaStorage = mediaStorage;
    }

    @Cacheable(value = "community-feed", key = "#mode + '-' + #page + '-' + #size + '-' + (#viewer != null ? #viewer.id : 'anon')")
    public CommunityFeedResponse getFeed(String mode, int page, int size, User viewer) {
        Page<CommunityPost> result;
        PageRequest pageable = PageRequest.of(page, Math.min(size, 30));

        if ("following".equalsIgnoreCase(mode) && viewer != null) {
            result = postRepository.findFollowingFeed(viewer.getId(), PostStatus.PUBLISHED, pageable);
        } else {
            result = postRepository.findByStatusOrderByTrendingScoreDescCreatedAtDesc(
                    PostStatus.PUBLISHED, pageable);
        }

        List<CommunityPostDto> posts = result.getContent().stream()
                .map(p -> mapper.toPostDto(p, viewer))
                .toList();

        CommunityPostDto featured = posts.isEmpty() ? null : posts.get(0);

        List<CommunityPostDto> topLiked = postRepository
                .findTop10ByStatusOrderByLikeCountDescCreatedAtDesc(PostStatus.PUBLISHED)
                .stream()
                .limit(6)
                .map(p -> mapper.toPostDto(p, viewer))
                .toList();

        List<com.oauth.demo.community.dto.AuthorSummaryDto> trendingCreators =
                profileRepository.findTop12ByOrderByTotalLikesDescTotalPostsDesc().stream()
                        .map(p -> mapper.toAuthorSummary(p.getUser(), viewer))
                        .toList();

        return new CommunityFeedResponse(
                posts,
                featured,
                topLiked,
                trendingCreators,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages(),
                result.hasNext()
        );
    }

    public CommunityFeedResponse getPostsByAuthor(Long authorId, User viewer, int page, int size) {
        Page<CommunityPost> result = postRepository.findByStatusAndAuthorIdOrderByCreatedAtDesc(
                PostStatus.PUBLISHED, authorId, PageRequest.of(page, Math.min(size, 30)));

        List<CommunityPostDto> posts = result.getContent().stream()
                .map(p -> mapper.toPostDto(p, viewer))
                .toList();

        return new CommunityFeedResponse(
                posts, null, List.of(), List.of(),
                page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext()
        );
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public CommunityPostDto createPost(
            User author,
            String title,
            String content,
            String postType,
            String tags,
            String portfolioLink,
            boolean draft,
            MultipartFile media,
            MultipartFile thumbnail
    ) throws IOException {
        ensureProfile(author);

        CommunityPost post = new CommunityPost();
        post.setAuthor(author);
        post.setTitle(title != null && !title.isBlank() ? title : "Untitled");
        post.setContent(content);
        post.setTags(tags);
        post.setPortfolioLink(portfolioLink);
        post.setStatus(draft ? PostStatus.DRAFT : PostStatus.PUBLISHED);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        PostType type = parsePostType(postType);
        post.setPostType(type);

        if (media != null && !media.isEmpty()) {
            CommunityMediaStorageService.StoredMedia stored = mediaStorage.store(media, author.getId());
            if (stored != null) {
                post.setMediaUrl(stored.mediaUrl());
                post.setThumbnailUrl(stored.thumbnailUrl());
                if (stored.video()) {
                    post.setPostType(PostType.VIDEO);
                } else if (stored.image()) {
                    post.setPostType(PostType.IMAGE);
                }
            }
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            CommunityMediaStorageService.StoredMedia thumb = mediaStorage.store(thumbnail, author.getId());
            if (thumb != null) {
                post.setThumbnailUrl(thumb.thumbnailUrl());
            }
        }

        if (type == PostType.PORTFOLIO) {
            post.setPostType(PostType.PORTFOLIO);
        }

        post.setTrendingScore(CommunityFeedRanker.computeScore(post));
        CommunityPost saved = postRepository.save(post);

        CreatorProfile profile = profileRepository.findByUserId(author.getId()).orElse(null);
        if (profile != null && saved.getStatus() == PostStatus.PUBLISHED) {
            profile.setTotalPosts(profile.getTotalPosts() + 1);
            profileRepository.save(profile);
        }

        return mapper.toPostDto(saved, author);
    }

    @Transactional
    public void incrementViewCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setViewCount(post.getViewCount() + 1);
            post.setTrendingScore(CommunityFeedRanker.computeScore(post));
            postRepository.save(post);
        });
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public void refreshTrendingScores() {
        postRepository.findAll().forEach(post -> {
            post.setTrendingScore(CommunityFeedRanker.computeScore(post));
            postRepository.save(post);
        });
    }

    private void ensureProfile(User author) {
        profileRepository.findByUserId(author.getId()).orElseGet(() -> {
            CreatorProfile profile = new CreatorProfile();
            profile.setUser(author);
            profile.setCreatedAt(LocalDateTime.now());
            profile.setUpdatedAt(LocalDateTime.now());
            return profileRepository.save(profile);
        });
    }

    private PostType parsePostType(String raw) {
        if (raw == null) return PostType.TEXT;
        try {
            return PostType.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PostType.TEXT;
        }
    }
}
