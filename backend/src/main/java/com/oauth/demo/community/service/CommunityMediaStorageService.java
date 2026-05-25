package com.oauth.demo.community.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class CommunityMediaStorageService {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm", "video/quicktime"
    );

    private final Path uploadRoot;

    public CommunityMediaStorageService(
            @Value("${community.media.upload-dir:uploads/community}") String uploadDir) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create upload directory: " + uploadRoot, ex);
        }
    }

    public StoredMedia store(MultipartFile file, Long userId) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String ext = extensionFor(contentType, file.getOriginalFilename());
        String filename = userId + "_" + UUID.randomUUID() + ext;

        Path userDir = uploadRoot.resolve(String.valueOf(userId));
        Files.createDirectories(userDir);
        Path target = userDir.resolve(filename);
        Files.copy(file.getInputStream(), target);

        String publicUrl = "/api/community/media/" + userId + "/" + filename;
        boolean video = VIDEO_TYPES.contains(contentType);
        boolean image = IMAGE_TYPES.contains(contentType);

        return new StoredMedia(publicUrl, publicUrl, video, image);
    }

    public Path resolveMediaPath(String userId, String filename) {
        Path resolved = uploadRoot.resolve(userId).resolve(filename).normalize();
        if (!resolved.startsWith(uploadRoot)) {
            throw new SecurityException("Invalid media path");
        }
        return resolved;
    }

    private String extensionFor(String contentType, String originalName) {
        if (originalName != null && originalName.contains(".")) {
            return originalName.substring(originalName.lastIndexOf('.'));
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            case "video/webm" -> ".webm";
            case "video/quicktime" -> ".mov";
            default -> ".jpg";
        };
    }

    public record StoredMedia(String mediaUrl, String thumbnailUrl, boolean video, boolean image) {}
}
