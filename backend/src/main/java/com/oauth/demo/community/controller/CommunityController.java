package com.oauth.demo.community.controller;

import com.oauth.demo.community.dto.CommentDto;
import com.oauth.demo.community.dto.CommunityFeedResponse;
import com.oauth.demo.community.dto.CommunityPostDto;
import com.oauth.demo.community.dto.CreatorProfileDto;
import com.oauth.demo.community.service.*;
import com.oauth.demo.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityPostService postService;
    private final CommunityInteractionService interactionService;
    private final CreatorProfileService profileService;
    private final CommunityUserService userService;

    public CommunityController(
            CommunityPostService postService,
            CommunityInteractionService interactionService,
            CreatorProfileService profileService,
            CommunityUserService userService) {
        this.postService = postService;
        this.interactionService = interactionService;
        this.profileService = profileService;
        this.userService = userService;
    }

    @GetMapping("/feed")
    public CommunityFeedResponse getFeed(
            @RequestParam(defaultValue = "trending") String mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Authentication auth
    ) {
        return postService.getFeed(mode, page, size, userService.optionalUser(auth));
    }

    @GetMapping("/posts/{postId}")
    public CommunityPostDto getPost(@PathVariable Long postId, Authentication auth) {
        return postService.getPost(postId, userService.optionalUser(auth));
    }

    @GetMapping("/posts/me")
    public CommunityFeedResponse myPosts(
            @RequestParam(defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        return postService.getMyPosts(userService.requireUser(auth), status, page, size);
    }

    @GetMapping("/bookmarks")
    public CommunityFeedResponse bookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        return postService.getBookmarks(userService.requireUser(auth), page, size);
    }

    @PostMapping("/posts")
    public CommunityPostDto createPost(
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam(defaultValue = "TEXT") String postType,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String portfolioLink,
            @RequestParam(required = false) String externalLink,
            @RequestParam(defaultValue = "false") boolean draft,
            @RequestParam(required = false) MultipartFile media,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile[] mediaFiles,
            Authentication auth
    ) throws IOException {
        User user = userService.requireUser(auth);
        return postService.createPost(
                user, title, content, postType, tags, portfolioLink, externalLink,
                draft, media, thumbnail, mediaFiles);
    }

    @PatchMapping("/posts/{postId}")
    public CommunityPostDto updatePost(
            @PathVariable Long postId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String portfolioLink,
            @RequestParam(required = false) String externalLink,
            @RequestParam(required = false) Boolean draft,
            @RequestParam(required = false) MultipartFile media,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile[] mediaFiles,
            Authentication auth
    ) throws IOException {
        return postService.updatePost(
                userService.requireUser(auth), postId, title, content, postType, tags,
                portfolioLink, externalLink, draft, media, thumbnail, mediaFiles);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId, Authentication auth) {
        postService.deletePost(userService.requireUser(auth), postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/publish")
    public CommunityPostDto publishDraft(@PathVariable Long postId, Authentication auth) {
        return postService.publishDraft(userService.requireUser(auth), postId);
    }

    @PostMapping("/posts/{postId}/repost")
    public CommunityPostDto repost(
            @PathVariable Long postId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth
    ) {
        String quote = body != null ? body.get("quote") : null;
        return postService.repost(userService.requireUser(auth), postId, quote);
    }

    @PostMapping("/posts/{postId}/view")
    public ResponseEntity<Void> recordView(@PathVariable Long postId) {
        postService.incrementViewCount(postId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/posts/{postId}/like")
    public Map<String, Object> toggleLike(@PathVariable Long postId, Authentication auth) {
        User user = userService.requireUser(auth);
        boolean liked = interactionService.toggleLike(user, postId);
        return Map.of("liked", liked);
    }

    @PostMapping("/posts/{postId}/bookmark")
    public Map<String, Object> toggleBookmark(@PathVariable Long postId, Authentication auth) {
        User user = userService.requireUser(auth);
        boolean saved = interactionService.toggleBookmark(user, postId);
        return Map.of("bookmarked", saved);
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentDto> getComments(@PathVariable Long postId, Authentication auth) {
        return interactionService.getComments(postId, userService.optionalUser(auth));
    }

    @PostMapping("/posts/{postId}/comments")
    public CommentDto addComment(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> body,
            Authentication auth
    ) {
        User user = userService.requireUser(auth);
        String content = (String) body.get("content");
        Long parentId = body.get("parentId") != null
                ? Long.valueOf(body.get("parentId").toString()) : null;
        return interactionService.addComment(user, postId, content, parentId);
    }

    @GetMapping("/creators/trending")
    public List<com.oauth.demo.community.dto.AuthorSummaryDto> trendingCreators(Authentication auth) {
        return profileService.getTrendingCreators(userService.optionalUser(auth));
    }

    @GetMapping("/profiles/{username}")
    public CreatorProfileDto getProfile(@PathVariable String username, Authentication auth) {
        return profileService.getProfile(username, userService.optionalUser(auth));
    }

    @GetMapping("/profiles/me")
    public CreatorProfileDto myProfile(Authentication auth) {
        User user = userService.requireUser(auth);
        return profileService.getProfile(user.getUsername(), user);
    }

    @PatchMapping("/profiles/me")
    public CreatorProfileDto updateProfile(
            @RequestBody CreatorProfileService.CreatorProfileUpdate update,
            Authentication auth
    ) {
        User user = userService.requireUser(auth);
        return profileService.updateMyProfile(user, update);
    }

    @PostMapping("/users/{userId}/follow")
    public Map<String, Object> toggleFollow(@PathVariable Long userId, Authentication auth) {
        User user = userService.requireUser(auth);
        boolean following = interactionService.toggleFollow(user, userId);
        return Map.of("following", following);
    }
}
