package com.oauth.demo.reddit.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the highest-quality preview URLs from Reddit post JSON.
 */
public final class RedditImageResolver {

    private static final Set<String> INVALID_THUMBNAILS = Set.of(
            "", "self", "default", "nsfw", "spoiler", "image", "deleted", "blocked"
    );

    private static final Pattern YOUTUBE_ID = Pattern.compile(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/shorts/)([a-zA-Z0-9_-]{6,})"
    );

    private RedditImageResolver() {}

    public record ResolvedImage(String primaryUrl, List<String> fallbackUrls, String mediaType) {}

    public static ResolvedImage resolve(JsonNode data) {
        List<ScoredUrl> candidates = new ArrayList<>();
        collectAllCandidates(data, candidates);

        if (candidates.isEmpty()) {
            return new ResolvedImage(null, List.of(), detectMediaType(data));
        }

        candidates.sort(Comparator.comparingInt(ScoredUrl::width).reversed());

        LinkedHashSet<String> ordered = new LinkedHashSet<>();
        for (ScoredUrl c : candidates) {
            String upgraded = upgradeImageUrl(c.url());
            if (upgraded != null && !upgraded.isBlank()) {
                ordered.add(upgraded);
            }
        }

        List<String> urls = new ArrayList<>(ordered);
        String primary = urls.isEmpty() ? null : urls.get(0);
        List<String> fallbacks = urls.size() > 1 ? urls.subList(1, urls.size()) : List.of();

        return new ResolvedImage(primary, fallbacks, detectMediaType(data));
    }

    private static void collectAllCandidates(JsonNode data, List<ScoredUrl> out) {
        addPreviewImages(data, out);
        addRedditVideoPreview(data, out);
        addGalleryImages(data, out);
        addOembedThumbnails(data, out);
        addLinkImages(data, out);
        addYoutubeThumbnail(data, out);

        String thumb = decodeUrl(data.path("thumbnail").asText(""));
        if (isValidHttpUrl(thumb) && data.path("thumbnail_width").asInt(0) >= 90) {
            out.add(new ScoredUrl(upgradeImageUrl(thumb), data.path("thumbnail_width").asInt(0)));
        }

        JsonNode crossposts = data.path("crosspost_parent_list");
        if (crossposts.isArray()) {
            for (JsonNode cross : crossposts) {
                JsonNode crossData = cross.path("data");
                if (!crossData.isMissingNode()) {
                    collectAllCandidates(crossData, out);
                }
            }
        }
    }

    private static void addPreviewImages(JsonNode data, List<ScoredUrl> out) {
        JsonNode images = data.path("preview").path("images");
        if (!images.isArray()) {
            return;
        }
        for (JsonNode image : images) {
            JsonNode source = image.path("source");
            String sourceUrl = decodeUrl(source.path("url").asText(""));
            int sourceW = source.path("width").asInt(0);
            if (!sourceUrl.isBlank()) {
                out.add(new ScoredUrl(sourceUrl, Math.max(sourceW, 1080)));
            }
            JsonNode resolutions = image.path("resolutions");
            if (resolutions.isArray()) {
                for (JsonNode res : resolutions) {
                    String url = decodeUrl(res.path("url").asText(""));
                    int w = res.path("width").asInt(0);
                    if (!url.isBlank()) {
                        out.add(new ScoredUrl(url, w));
                    }
                }
            }
            JsonNode variants = image.path("variants");
            addVariantUrl(variants.path("obfuscated"), out);
            addVariantUrl(variants.path("nsfw"), out);
        }
    }

    private static void addVariantUrl(JsonNode variant, List<ScoredUrl> out) {
        if (variant.isMissingNode()) {
            return;
        }
        String url = decodeUrl(variant.path("source").path("url").asText(""));
        int w = variant.path("source").path("width").asInt(0);
        if (!url.isBlank()) {
            out.add(new ScoredUrl(url, w));
        }
    }

    private static void addRedditVideoPreview(JsonNode data, List<ScoredUrl> out) {
        String fallback = decodeUrl(data.path("preview").path("reddit_video_preview").path("fallback_url").asText(""));
        if (!fallback.isBlank()) {
            out.add(new ScoredUrl(fallback, 1280));
        }
        JsonNode redditVideo = data.path("media").path("reddit_video");
        if (!redditVideo.isMissingNode()) {
            String poster = decodeUrl(redditVideo.path("fallback_url").asText(""));
            if (poster.contains("preview.redd.it") || poster.contains("i.redd.it")) {
                out.add(new ScoredUrl(poster, 1280));
            }
        }
        JsonNode secureVideo = data.path("secure_media").path("reddit_video");
        if (!secureVideo.isMissingNode()) {
            String poster = decodeUrl(secureVideo.path("fallback_url").asText(""));
            if (poster.contains("preview.redd.it") || poster.contains("i.redd.it")) {
                out.add(new ScoredUrl(poster, 1280));
            }
        }
    }

    private static void addGalleryImages(JsonNode data, List<ScoredUrl> out) {
        if (!data.path("is_gallery").asBoolean(false)) {
            return;
        }
        JsonNode items = data.path("gallery_data").path("items");
        JsonNode metadata = data.path("media_metadata");
        if (!items.isArray() || metadata.isMissingNode()) {
            return;
        }
        for (JsonNode item : items) {
            String mediaId = item.path("media_id").asText("");
            if (mediaId.isBlank()) {
                continue;
            }
            JsonNode media = metadata.path(mediaId);
            String source = decodeUrl(media.path("s").path("u").asText(""));
            int w = media.path("s").path("x").asInt(1080);
            if (!source.isBlank()) {
                out.add(new ScoredUrl(source, w));
            }
            JsonNode previews = media.path("p");
            if (previews.isArray()) {
                for (JsonNode p : previews) {
                    String url = decodeUrl(p.path("u").asText(""));
                    int pw = p.path("x").asInt(0);
                    if (!url.isBlank()) {
                        out.add(new ScoredUrl(url, pw));
                    }
                }
            }
        }
    }

    private static void addOembedThumbnails(JsonNode data, List<ScoredUrl> out) {
        for (String mediaKey : new String[] { "media", "secure_media" }) {
            String thumb = decodeUrl(data.path(mediaKey).path("oembed").path("thumbnail_url").asText(""));
            if (!thumb.isBlank()) {
                int w = data.path(mediaKey).path("oembed").path("thumbnail_width").asInt(640);
                out.add(new ScoredUrl(thumb, w));
            }
        }
    }

    private static void addLinkImages(JsonNode data, List<ScoredUrl> out) {
        for (String field : new String[] { "url_overridden_by_dest", "url" }) {
            String url = decodeUrl(data.path(field).asText(""));
            if (isDirectImageUrl(url)) {
                out.add(new ScoredUrl(url, 900));
            }
        }
    }

    private static void addYoutubeThumbnail(JsonNode data, List<ScoredUrl> out) {
        String url = decodeUrl(firstNonBlank(
                data.path("url_overridden_by_dest").asText(""),
                data.path("url").asText("")
        ));
        Matcher m = YOUTUBE_ID.matcher(url);
        if (m.find()) {
            String videoId = m.group(1);
            out.add(new ScoredUrl("https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg", 480));
            out.add(new ScoredUrl("https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg", 1280));
        }
    }

    private static String detectMediaType(JsonNode data) {
        if (data.path("is_gallery").asBoolean(false)) {
            return "gallery";
        }
        if (!data.path("media").path("reddit_video").isMissingNode()
                || !data.path("secure_media").path("reddit_video").isMissingNode()) {
            return "video";
        }
        String hint = data.path("post_hint").asText("");
        if (hint.contains("image")) {
            return "image";
        }
        if (hint.contains("hosted:video") || hint.contains("rich:video")) {
            return "video";
        }
        if (data.path("preview").path("images").isArray()
                && !data.path("preview").path("images").isEmpty()) {
            return "image";
        }
        return "link";
    }

    private static boolean isDirectImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        return lower.contains("i.redd.it")
                || lower.contains("preview.redd.it")
                || lower.contains("external-preview.redd.it")
                || lower.contains("i.imgur.com")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".png")
                || lower.endsWith(".webp")
                || lower.endsWith(".gif");
    }

    private static boolean isValidHttpUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        String lower = url.toLowerCase(Locale.ROOT);
        if (INVALID_THUMBNAILS.contains(lower)) {
            return false;
        }
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    static String upgradeImageUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String decoded = decodeUrl(url);
        String lower = decoded.toLowerCase(Locale.ROOT);

        if (lower.contains("preview.redd.it") || lower.contains("external-preview.redd.it")) {
            if (decoded.contains("width=")) {
                return decoded.replaceAll("(?i)width=\\d+", "width=1080");
            }
            return decoded + (decoded.contains("?") ? "&" : "?") + "width=1080";
        }
        if (lower.contains("i.redd.it")) {
            if (!decoded.contains("width=")) {
                return decoded + (decoded.contains("?") ? "&" : "?") + "width=1080";
            }
        }
        return decoded;
    }

    private static String decodeUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        return url.replace("&amp;", "&").trim();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return "";
    }

    private record ScoredUrl(String url, int width) {}
}
