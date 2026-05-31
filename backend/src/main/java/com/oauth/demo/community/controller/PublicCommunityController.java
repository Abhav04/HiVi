package com.oauth.demo.community.controller;

import com.oauth.demo.community.config.CommunityDemoSeeder;
import com.oauth.demo.community.dto.CommentDto;
import com.oauth.demo.community.dto.CommunityFeedResponse;
import com.oauth.demo.community.dto.CommunityPostDto;
import com.oauth.demo.community.dto.CreatorProfileDto;
import com.oauth.demo.community.repository.CommunityPostRepository;
import com.oauth.demo.community.service.CommunityInteractionService;
import com.oauth.demo.community.service.CommunityPostService;
import com.oauth.demo.community.service.CommunityUserService;
import com.oauth.demo.community.service.CreatorProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Public read-only community APIs — always accessible without JWT.
 * Used by the web app feed to avoid auth misconfiguration blocking core UX.
 */
@RestController
@RequestMapping("/api/public/community")
public class PublicCommunityController {

    private static final Logger log = LoggerFactory.getLogger(PublicCommunityController.class);

    private final CommunityPostService postService;
    private final CommunityInteractionService interactionService;
    private final CreatorProfileService profileService;
    private final CommunityUserService userService;
    private final CommunityDemoSeeder demoSeeder;
    private final CommunityPostRepository postRepository;

    public PublicCommunityController(
            CommunityPostService postService,
            CommunityInteractionService interactionService,
            CreatorProfileService profileService,
            CommunityUserService userService,
            CommunityDemoSeeder demoSeeder,
            CommunityPostRepository postRepository) {
        this.postService = postService;
        this.interactionService = interactionService;
        this.profileService = profileService;
        this.userService = userService;
        this.demoSeeder = demoSeeder;
        this.postRepository = postRepository;
    }

    @GetMapping("/feed")
    public CommunityFeedResponse getFeed(
            @RequestParam(defaultValue = "trending") String mode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Authentication auth
    ) {
        ensureDemoContent();
        return postService.getFeed(mode, page, size, userService.optionalUser(auth));
    }

    @GetMapping("/posts/{postId}")
    public CommunityPostDto getPost(@PathVariable Long postId, Authentication auth) {
        return postService.getPost(postId, userService.optionalUser(auth));
    }

    @GetMapping("/posts/{postId}/comments")
    public List<CommentDto> getComments(@PathVariable Long postId, Authentication auth) {
        return interactionService.getComments(postId, userService.optionalUser(auth));
    }

    @GetMapping("/profiles/{username}")
    public CreatorProfileDto getProfile(@PathVariable String username, Authentication auth) {
        return profileService.getProfile(username, userService.optionalUser(auth));
    }

    /** Idempotent: seeds demo creators/posts when the database is empty. */
    @PostMapping("/ensure-demo")
    public ResponseEntity<Map<String, Object>> ensureDemo() {
        long before = postRepository.count();
        if (before == 0) {
            demoSeeder.runSeed();
        }
        return ResponseEntity.ok(Map.of(
                "posts", postRepository.count(),
                "seeded", before == 0
        ));
    }

    private void ensureDemoContent() {
        try {
            if (postRepository.count() == 0) {
                demoSeeder.runSeed();
                log.info("Community demo seed completed — {} posts", postRepository.count());
            }
        } catch (Exception ex) {
            log.error("Community demo seed failed (feed may still work if posts exist)", ex);
        }
    }
}
