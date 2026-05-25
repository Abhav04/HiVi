package com.oauth.demo.community.service;

import com.oauth.demo.community.dto.CommentDto;
import com.oauth.demo.community.entity.*;
import com.oauth.demo.community.repository.*;
import com.oauth.demo.entity.User;
import com.oauth.demo.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommunityInteractionService {

    private final PostLikeRepository likeRepository;
    private final PostBookmarkRepository bookmarkRepository;
    private final PostCommentRepository commentRepository;
    private final CommunityPostRepository postRepository;
    private final CreatorFollowRepository followRepository;
    private final CommunityMapper mapper;
    private final UserRepository userRepository;

    public CommunityInteractionService(
            PostLikeRepository likeRepository,
            PostBookmarkRepository bookmarkRepository,
            PostCommentRepository commentRepository,
            CommunityPostRepository postRepository,
            CreatorFollowRepository followRepository,
            CommunityMapper mapper,
            UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public boolean toggleLike(User user, Long postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        var existing = likeRepository.findByUserIdAndPostId(user.getId(), postId);
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
            post.setTrendingScore(CommunityFeedRanker.computeScore(post));
            postRepository.save(post);
            return false;
        }

        PostLike like = new PostLike();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);
        post.setLikeCount(post.getLikeCount() + 1);
        post.setTrendingScore(CommunityFeedRanker.computeScore(post));
        postRepository.save(post);
        return true;
    }

    @Transactional
    public boolean toggleBookmark(User user, Long postId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        var existing = bookmarkRepository.findByUserIdAndPostId(user.getId(), postId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            post.setBookmarkCount(Math.max(0, post.getBookmarkCount() - 1));
            postRepository.save(post);
            return false;
        }

        PostBookmark bookmark = new PostBookmark();
        bookmark.setUser(user);
        bookmark.setPost(post);
        bookmarkRepository.save(bookmark);
        post.setBookmarkCount(post.getBookmarkCount() + 1);
        postRepository.save(post);
        return true;
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public CommentDto addComment(User user, Long postId, String content, Long parentId) {
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setAuthor(user);
        comment.setContent(content);

        if (parentId != null) {
            PostComment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            comment.setParent(parent);
        }

        PostComment saved = commentRepository.save(comment);
        post.setCommentCount((int) commentRepository.countByPostId(postId));
        post.setTrendingScore(CommunityFeedRanker.computeScore(post));
        postRepository.save(post);

        return mapper.toCommentDto(saved, user, List.of());
    }

    public List<CommentDto> getComments(Long postId, User viewer) {
        List<PostComment> roots = commentRepository.findByPostIdAndParentIsNullOrderByCreatedAtDesc(postId);
        List<CommentDto> result = new ArrayList<>();
        for (PostComment root : roots) {
            List<CommentDto> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(root.getId())
                    .stream()
                    .map(r -> mapper.toCommentDto(r, viewer, List.of()))
                    .toList();
            result.add(mapper.toCommentDto(root, viewer, replies));
        }
        return result;
    }

    @Transactional
    @CacheEvict(value = "community-feed", allEntries = true)
    public boolean toggleFollow(User follower, Long followingId) {
        if (follower.getId().equals(followingId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        var existing = followRepository.findByFollowerIdAndFollowingId(follower.getId(), followingId);
        if (existing.isPresent()) {
            followRepository.delete(existing.get());
            return false;
        }

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CreatorFollow follow = new CreatorFollow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
        return true;
    }
}
