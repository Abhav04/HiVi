package com.oauth.demo.community.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommentDto(
        Long id,
        String content,
        int likeCount,
        AuthorSummaryDto author,
        LocalDateTime createdAt,
        List<CommentDto> replies
) {}
