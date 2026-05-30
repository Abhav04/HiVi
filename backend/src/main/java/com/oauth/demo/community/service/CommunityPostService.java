package com.oauth.demo.community.service;

import com.oauth.demo.community.dto.CommunityFeedResponse;
import com.oauth.demo.community.dto.CommunityPostDto;
import com.oauth.demo.community.entity.CommunityPost;
import com.oauth.demo.community.entity.CreatorProfile;
import com.oauth.demo.community.entity.PostMedia;
import com.oauth.demo.community.entity.PostStatus;
import com.oauth.demo.community.entity.PostType;
import com.oauth.demo.community.repository.CommunityPostRepository;
import com.oauth.demo.community.repository.CreatorProfileRepository;
import com.oauth.demo.community.repository.PostBookmarkRepository;
import com.oauth.demo.community.repository.PostMediaRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
public class CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final CreatorProfileRepository profileRepository;
    private final PostBookmarkRepository bookmarkRepository;
    private final PostMediaRepository mediaRepository;
    private final CommunityMapper mapper;
    private final CommunityMediaStorageService mediaStorage;

    public CommunityPostService(
            CommunityPostRepository postRepository,
            CreatorProfileRepository profileRepository,
            PostBookmarkRepository bookmarkRepository,
            PostMediaRepository mediaRepository,
            CommunityMapper mapper,
            CommunityMediaStorageService mediaStorage) {
        this.postRepository = postRepository;
        this.profileRepository = profileRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.mediaRepository = mediaRepository;
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

    public CommunityPostDto getPost(Long postId, User viewer) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (post.getStatus() != PostStatus.PUBLISHED && !isAuthor(post, viewer)) {
            throw new IllegalArgumentException("Post not available");
        }
        return mapper.toPostDto(post, viewer);
    }

    public CommunityFeedResponse getMyPosts(User author, String statusFilter, int page, int size) {
        Page<CommunityPost> result = resolveAuthorPostsPage(author.getId(), statusFilter, page, size);
        return toFeedPage(result, author, page, size);
    }

    public CommunityFeedResponse getBookmarks(User viewer, int page, int size) {
        Page<CommunityPost> result = bookmarkRepository.findBookmarkedPosts(
                viewer.getId(), PageRequest.of(page, Math.min(size, 30)));
        return toFeedPage(result, viewer, page, size);
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
            String externalLink,
            boolean draft,
            MultipartFile media,
            MultipartFile thumbnail,
            MultipartFile[] mediaFiles
    ) throws IOException {
        ensureProfile(author);

        CommunityPost post = new CommunityPost();
        post.setAuthor(author);
        post.setTitle(title != null && !title.isBlank() ? title : "Untitled");
        post.setContent(content);
        post.setTags(normalizeTags(tags));
        post.setPortfolioLink(portfolioLink);
        post.setExternalLink(externalLink);
        post.setStatus(draft ? PostStatus.DRAFT : PostStatus.PUBLISHED);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setPostType(parsePostType(postType));

        applyUploadedMedia(post, author.getId(), media, thumbnail, mediaFiles);

        if (post.getPostType() == PostType.PORTFOLIO || post.getPostType() == PostType.PORTFOLIO_SHOWCASE) {
            post.setPostType(post.getPostType() == PostType.PORTFOLIO_SHOWCASE
                    ? PostType.PORTFOLIO_SHOWCASE : PostType.PORTFOLIO);
        }

        post.setTrendingScore(CommunityFeedRanker.computeScore(post));
        CommunityPost saved = postRepository.save(post);

        if (saved.getStatus() == PostStatus.PUBLISHED) {
            incrementProfilePostCount(author.getId());
        }

        return mapper.toPostDto(saved, author);
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public CommunityPostDto updatePost(
            User author,
            Long postId,
            String title,
            String content,
            String postType,
            String tags,
            String portfolioLink,
            String externalLink,
            Boolean draft,
            MultipartFile media,
            MultipartFile thumbnail,
            MultipartFile[] mediaFiles
    ) throws IOException {
        CommunityPost post = requireOwnedPost(author, postId);
        boolean wasDraft = post.getStatus() == PostStatus.DRAFT;

        if (title != null && !title.isBlank()) post.setTitle(title);
        if (content != null) post.setContent(content);
        if (tags != null) post.setTags(normalizeTags(tags));
        if (portfolioLink != null) post.setPortfolioLink(portfolioLink);
        if (externalLink != null) post.setExternalLink(externalLink);
        if (postType != null) post.setPostType(parsePostType(postType));
        if (draft != null) {
            post.setStatus(draft ? PostStatus.DRAFT : PostStatus.PUBLISHED);
        }

        if (mediaFiles != null && mediaFiles.length > 0 || (media != null && !media.isEmpty())) {
            post.getMediaItems().clear();
            mediaRepository.deleteByPostId(post.getId());
            applyUploadedMedia(post, author.getId(), media, thumbnail, mediaFiles);
        }

        post.setUpdatedAt(LocalDateTime.now());
        post.setTrendingScore(CommunityFeedRanker.computeScore(post));
        CommunityPost saved = postRepository.save(post);

        if (wasDraft && saved.getStatus() == PostStatus.PUBLISHED) {
            incrementProfilePostCount(author.getId());
        }

        return mapper.toPostDto(saved, author);
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public void deletePost(User author, Long postId) {
        CommunityPost post = requireOwnedPost(author, postId);
        if (post.getStatus() == PostStatus.PUBLISHED) {
            decrementProfilePostCount(author.getId());
        }
        mediaRepository.deleteByPostId(post.getId());
        postRepository.delete(post);
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public CommunityPostDto publishDraft(User author, Long postId) {
        CommunityPost post = requireOwnedPost(author, postId);
        if (post.getStatus() != PostStatus.DRAFT) {
            throw new IllegalArgumentException("Post is not a draft");
        }
        post.setStatus(PostStatus.PUBLISHED);
        post.setUpdatedAt(LocalDateTime.now());
        post.setTrendingScore(CommunityFeedRanker.computeScore(post));
        CommunityPost saved = postRepository.save(post);
        incrementProfilePostCount(author.getId());
        return mapper.toPostDto(saved, author);
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public CommunityPostDto repost(User author, Long originalPostId, String quote) {
        CommunityPost original = postRepository.findById(originalPostId)
                .orElseThrow(() -> new IllegalArgumentException("Original post not found"));
        if (original.getStatus() != PostStatus.PUBLISHED) {
            throw new IllegalArgumentException("Cannot repost unpublished content");
        }

        ensureProfile(author);

        CommunityPost repost = new CommunityPost();
        repost.setAuthor(author);
        repost.setOriginalPost(original);
        repost.setPostType(PostType.REPOST);
        repost.setTitle(original.getTitle());
        repost.setContent(quote != null && !quote.isBlank() ? quote : null);
        repost.setTags(original.getTags());
        repost.setStatus(PostStatus.PUBLISHED);
        repost.setCreatedAt(LocalDateTime.now());
        repost.setUpdatedAt(LocalDateTime.now());
        repost.setTrendingScore(CommunityFeedRanker.computeScore(repost));

        original.setRepostCount(original.getRepostCount() + 1);
        postRepository.save(original);

        CommunityPost saved = postRepository.save(repost);
        incrementProfilePostCount(author.getId());
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

    private void applyUploadedMedia(
            CommunityPost post,
            Long userId,
            MultipartFile media,
            MultipartFile thumbnail,
            MultipartFile[] mediaFiles
    ) throws IOException {
        List<MultipartFile> files = new ArrayList<>();
        if (mediaFiles != null) {
            for (MultipartFile f : mediaFiles) {
                if (f != null && !f.isEmpty()) files.add(f);
            }
        }
        if (files.isEmpty() && media != null && !media.isEmpty()) {
            files.add(media);
        }

        int order = 0;
        for (MultipartFile file : files) {
            if (order >= 10) break;
            CommunityMediaStorageService.StoredMedia stored = mediaStorage.store(file, userId);
            if (stored == null) continue;

            PostMedia item = new PostMedia();
            item.setPost(post);
            item.setMediaUrl(stored.mediaUrl());
            item.setThumbnailUrl(stored.thumbnailUrl());
            item.setMediaKind(stored.video()
                    ? PostMedia.MediaKind.VIDEO
                    : PostMedia.MediaKind.IMAGE);
            item.setSortOrder(order++);
            post.getMediaItems().add(item);

            if (order == 1) {
                post.setMediaUrl(stored.mediaUrl());
                post.setThumbnailUrl(stored.thumbnailUrl());
                if (stored.video()) post.setPostType(PostType.VIDEO);
                else if (stored.image() && post.getPostType() == PostType.TEXT) {
                    post.setPostType(PostType.IMAGE);
                }
            }
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {
            CommunityMediaStorageService.StoredMedia thumb = mediaStorage.store(thumbnail, userId);
            if (thumb != null) {
                post.setThumbnailUrl(thumb.thumbnailUrl());
                if (!post.getMediaItems().isEmpty()) {
                    post.getMediaItems().get(0).setThumbnailUrl(thumb.thumbnailUrl());
                }
            }
        }
    }

    private CommunityFeedResponse toFeedPage(Page<CommunityPost> result, User viewer, int page, int size) {
        List<CommunityPostDto> posts = result.getContent().stream()
                .map(p -> mapper.toPostDto(p, viewer))
                .toList();
        return new CommunityFeedResponse(
                posts, null, List.of(), List.of(),
                page, size, result.getTotalElements(), result.getTotalPages(), result.hasNext()
        );
    }

    private Page<CommunityPost> resolveAuthorPostsPage(Long authorId, String statusFilter, int page, int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 30));
        if (statusFilter == null || statusFilter.isBlank() || "ALL".equalsIgnoreCase(statusFilter)) {
            return postRepository.findByAuthorIdOrderByUpdatedAtDesc(authorId, pageable);
        }
        PostStatus status = "DRAFT".equalsIgnoreCase(statusFilter) ? PostStatus.DRAFT : PostStatus.PUBLISHED;
        return postRepository.findByAuthorIdAndStatusOrderByUpdatedAtDesc(authorId, status, pageable);
    }

    private CommunityPost requireOwnedPost(User author, Long postId) {
        return postRepository.findByIdAndAuthorId(postId, author.getId())
                .orElseThrow(() -> new IllegalArgumentException("Post not found or not owned by you"));
    }

    private boolean isAuthor(CommunityPost post, User viewer) {
        return viewer != null && post.getAuthor().getId().equals(viewer.getId());
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

    private void incrementProfilePostCount(Long userId) {
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setTotalPosts(profile.getTotalPosts() + 1);
            profileRepository.save(profile);
        });
    }

    private void decrementProfilePostCount(Long userId) {
        profileRepository.findByUserId(userId).ifPresent(profile -> {
            profile.setTotalPosts(Math.max(0, profile.getTotalPosts() - 1));
            profileRepository.save(profile);
        });
    }

    private PostType parsePostType(String raw) {
        if (raw == null) return PostType.TEXT;
        try {
            return PostType.valueOf(raw.toUpperCase().replace('-', '_').replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return PostType.TEXT;
        }
    }

    private String normalizeTags(String tags) {
        if (tags == null) return null;
        return tags.replace("#", "").trim();
    }
}
