package com.oauth.demo.community.dto;

public record PostMediaDto(
        Long id,
        String mediaUrl,
        String thumbnailUrl,
        String mediaKind,
        int sortOrder
) {}
