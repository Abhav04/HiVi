package com.oauth.demo.community.dto;

public record RepostSummaryDto(
        Long id,
        String title,
        AuthorSummaryDto author
) {}
