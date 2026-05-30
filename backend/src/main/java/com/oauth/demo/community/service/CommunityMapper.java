package com.oauth.demo.community.service;

import com.oauth.demo.community.dto.*;
import com.oauth.demo.community.entity.CommunityPost;
import com.oauth.demo.community.entity.CreatorProfile;
import com.oauth.demo.community.entity.PostComment;
import com.oauth.demo.community.entity.PostMedia;
import com.oauth.demo.community.repository.CreatorFollowRepository;
import com.oauth.demo.community.repository.CreatorProfileRepository;
import com.oauth.demo.community.repository.PostBookmarkRepository;
import com.oauth.demo.community.repository.PostLikeRepository;
import com.oauth.demo.community.repository.PostMediaRepository;
import com.oauth.demo.entity.User;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class CommunityMapper {

    private final CreatorProfileRepository profileRepository;
    private final PostLikeRepository likeRepository;
    private final PostBookmarkRepository bookmarkRepository;
    private final CreatorFollowRepository followRepository;
    private final PostMediaRepository mediaRepository;

    public CommunityMapper(
            CreatorProfileRepository profileRepository,
            PostLikeRepository likeRepository,
            PostBookmarkRepository bookmarkRepository,
            CreatorFollowRepository followRepository,
            PostMediaRepository mediaRepository) {
        this.profileRepository = profileRepository;
        this.likeRepository = likeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.followRepository = followRepository;
        this.mediaRepository = mediaRepository;
    }

    public CommunityPostDto toPostDto(CommunityPost post, User viewer) {
        Long viewerId = viewer != null ? viewer.getId() : null;
        boolean liked = viewerId != null && likeRepository.existsByUserIdAndPostId(viewerId, post.getId());
        boolean bookmarked = viewerId != null && bookmarkRepository.existsByUserIdAndPostId(viewerId, post.getId());
        boolean owned = viewerId != null && post.getAuthor().getId().equals(viewerId);

        RepostSummaryDto repostOf = null;
        if (post.getOriginalPost() != null) {
            CommunityPost orig = post.getOriginalPost();
            repostOf = new RepostSummaryDto(
                    orig.getId(),
                    orig.getTitle(),
                    toAuthorSummary(orig.getAuthor(), viewer)
            );
        }

        return new CommunityPostDto(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPostType().name(),
                post.getStatus().name(),
                post.getMediaUrl(),
                post.getThumbnailUrl(),
                post.getPortfolioLink(),
                post.getExternalLink(),
                parseTags(post.getTags()),
                loadMediaDtos(post),
                repostOf,
                post.getRepostCount(),
                post.getLikeCount(),
                post.getCommentCount(),
                post.getViewCount(),
                post.getTrendingScore(),
                liked,
                bookmarked,
                owned,
                toAuthorSummary(post.getAuthor(), viewer),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    private List<PostMediaDto> loadMediaDtos(CommunityPost post) {
        List<PostMedia> items = post.getMediaItems();
        if (items == null || items.isEmpty()) {
            items = mediaRepository.findByPostIdOrderBySortOrderAsc(post.getId());
        }
        if (items.isEmpty() && post.getMediaUrl() != null) {
            return List.of(new PostMediaDto(
                    null,
                    post.getMediaUrl(),
                    post.getThumbnailUrl(),
                    post.getPostType() == com.oauth.demo.community.entity.PostType.VIDEO ? "VIDEO" : "IMAGE",
                    0
            ));
        }
        return items.stream().map(m -> new PostMediaDto(
                m.getId(),
                m.getMediaUrl(),
                m.getThumbnailUrl(),
                m.getMediaKind().name(),
                m.getSortOrder()
        )).toList();
    }

    public AuthorSummaryDto toAuthorSummary(User user, User viewer) {
        CreatorProfile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        boolean following = viewer != null
                && followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), user.getId());

        return new AuthorSummaryDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getNiche() : null,
                profile != null && profile.isAvailableForWork(),
                following
        );
    }

    public CommentDto toCommentDto(PostComment comment, User viewer, List<CommentDto> replies) {
        return new CommentDto(
                comment.getId(),
                comment.getContent(),
                comment.getLikeCount(),
                toAuthorSummary(comment.getAuthor(), viewer),
                comment.getCreatedAt(),
                replies != null ? replies : List.of()
        );
    }

    public CreatorProfileDto toProfileDto(
            CreatorProfile profile,
            User user,
            User viewer,
            List<CommunityPostDto> recentPosts) {
        boolean following = viewer != null
                && followRepository.existsByFollowerIdAndFollowingId(viewer.getId(), user.getId());

        return new CreatorProfileDto(
                user.getId(),
                user.getUsername(),
                user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                profile.getBio(),
                profile.getNiche(),
                profile.getTools(),
                profile.getAvatarUrl(),
                profile.getBannerUrl(),
                profile.getPortfolioUrl(),
                profile.getInstagramUrl(),
                profile.getYoutubeUrl(),
                profile.getWebsiteUrl(),
                profile.isAvailableForWork(),
                profile.getTotalPosts(),
                profile.getTotalLikes(),
                profile.getTotalViews(),
                followRepository.countByFollowingId(user.getId()),
                followRepository.countByFollowerId(user.getId()),
                following,
                recentPosts
        );
    }

    private List<String> parseTags(String tags) {
        if (tags == null || tags.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
