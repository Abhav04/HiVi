package com.oauth.demo.community.controller;

import com.oauth.demo.community.service.CommunityMediaStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/community/media")
public class CommunityMediaController {

    private final CommunityMediaStorageService mediaStorage;

    public CommunityMediaController(CommunityMediaStorageService mediaStorage) {
        this.mediaStorage = mediaStorage;
    }

    @GetMapping("/{userId}/{filename}")
    public ResponseEntity<Resource> serveMedia(
            @PathVariable String userId,
            @PathVariable String filename
    ) throws Exception {
        Path path = mediaStorage.resolveMediaPath(userId, filename);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(path.toUri());
        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(resource);
    }
}
