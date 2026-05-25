package com.oauth.demo.opportunities.controller;

import com.oauth.demo.community.service.CommunityUserService;
import com.oauth.demo.opportunities.dto.CreateOpportunityRequest;
import com.oauth.demo.opportunities.dto.OpportunityDto;
import com.oauth.demo.opportunities.dto.OpportunityFeedResponse;
import com.oauth.demo.opportunities.service.OpportunityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final CommunityUserService userService;

    public OpportunityController(OpportunityService opportunityService, CommunityUserService userService) {
        this.opportunityService = opportunityService;
        this.userService = userService;
    }

    @GetMapping
    public OpportunityFeedResponse list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source
    ) {
        opportunityService.ensureSeeded();
        return opportunityService.getFeed(page, size, category, source);
    }

    @GetMapping("/trending")
    public List<OpportunityDto> trending() {
        opportunityService.ensureSeeded();
        return opportunityService.getTrending();
    }

    @GetMapping("/category/{type}")
    public OpportunityFeedResponse byCategory(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        opportunityService.ensureSeeded();
        return opportunityService.getByCategory(type, page, size);
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestBody CreateOpportunityRequest request,
            Authentication auth
    ) {
        try {
            var dto = opportunityService.createUserOpportunity(
                    request, userService.requireUser(auth));
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }
}
