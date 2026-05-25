package com.oauth.demo.community.config;

import com.oauth.demo.community.entity.*;
import com.oauth.demo.community.repository.*;
import com.oauth.demo.community.service.CommunityFeedRanker;
import com.oauth.demo.entity.User;
import com.oauth.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo/seed data for local development. Enable with community.demo.seed=true (local profile).
 * Safe to disable in production — does not run unless property is set.
 */
@Component
@ConditionalOnProperty(name = "community.demo.seed", havingValue = "true")
public class CommunityDemoSeeder {

    private static final Logger log = LoggerFactory.getLogger(CommunityDemoSeeder.class);

    private final UserRepository userRepository;
    private final CreatorProfileRepository profileRepository;
    private final CommunityPostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final PostLikeRepository likeRepository;
    private final CreatorFollowRepository followRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${community.demo.seed.force:false}")
    private boolean forceReseed;

    public CommunityDemoSeeder(
            UserRepository userRepository,
            CreatorProfileRepository profileRepository,
            CommunityPostRepository postRepository,
            PostCommentRepository commentRepository,
            PostLikeRepository likeRepository,
            CreatorFollowRepository followRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.followRepository = followRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void runSeed() {
        if (postRepository.count() > 0 && !forceReseed) {
            log.info("Community demo data already present — skipping seed");
            return;
        }

        if (forceReseed && postRepository.count() > 0) {
            log.warn("community.demo.seed.force=true — clearing community demo tables");
            likeRepository.deleteAll();
            commentRepository.deleteAll();
            followRepository.deleteAll();
            postRepository.deleteAll();
        }

        log.info("Seeding HiVi community demo data…");

        List<DemoCreator> creators = List.of(
                new DemoCreator("cinematic_maya", "Maya Ray", "Cinematic & documentary colorist",
                        "cinematic, documentaries, color grading", "DaVinci Resolve, Premiere Pro",
                        "https://images.unsplash.com/photo-1574717024653-61fd2cf4d44d?w=800&q=80",
                        true),
                new DemoCreator("anime_kai", "Kai Tanaka", "Anime & AMV specialist",
                        "anime, amv, motion graphics", "After Effects, Premiere",
                        "https://images.unsplash.com/photo-1611162616475-46b635cb6868?w=800&q=80",
                        false),
                new DemoCreator("glitch_gamer", "Jordan Vex", "Gaming montage & hype edits",
                        "gaming, montage, esports", "Premiere Pro, After Effects",
                        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&q=80",
                        true),
                new DemoCreator("reel_riya", "Riya Sharma", "Short-form reel editor for brands",
                        "reels, social, lifestyle", "CapCut, Premiere Pro",
                        "https://images.unsplash.com/photo-1611532736597-de2d90e850f7?w=800&q=80",
                        true),
                new DemoCreator("motion_leo", "Leo Martins", "Motion graphics & title sequences",
                        "motion graphics, title design", "After Effects, Cinema 4D",
                        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=800&q=80",
                        false),
                new DemoCreator("freelance_dev", "Dev Patel", "Freelance editor — open for clients",
                        "freelance, corporate, explainers", "Premiere, DaVinci",
                        "https://images.unsplash.com/photo-1598488035139-bdbb2231d1bb?w=800&q=80",
                        true)
        );

        List<User> users = new ArrayList<>();
        for (DemoCreator dc : creators) {
            User user = userRepository.findByUsername(dc.username());
            if (user == null) {
                User u = new User();
                u.setUsername(dc.username());
                u.setEmail(dc.username() + "@demo.hivi.local");
                u.setDisplayName(dc.displayName());
                u.setPassword(passwordEncoder.encode("demo1234"));
                u.setProvider("LOCAL");
                u.setRole("EDITOR");
                u.setEnabled(true);
                user = userRepository.save(u);
            }
            final User creator = user;

            CreatorProfile profile = profileRepository.findByUserId(creator.getId()).orElseGet(() -> {
                CreatorProfile p = new CreatorProfile();
                p.setUser(creator);
                p.setBio(dc.bio());
                p.setNiche(dc.niche());
                p.setTools(dc.tools());
                p.setAvatarUrl(avatarUrl(dc.displayName()));
                p.setBannerUrl(dc.bannerUrl());
                p.setPortfolioUrl("https://portfolio.demo.hivi/" + dc.username());
                p.setAvailableForWork(dc.available());
                p.setInstagramUrl("https://instagram.com/" + dc.username());
                return profileRepository.save(p);
            });
            users.add(user);
        }

        List<DemoPost> demoPosts = buildDemoPosts();
        List<CommunityPost> savedPosts = new ArrayList<>();

        for (int i = 0; i < demoPosts.size(); i++) {
            DemoPost dp = demoPosts.get(i);
            User author = users.get(i % users.size());

            CommunityPost post = new CommunityPost();
            post.setAuthor(author);
            post.setTitle(dp.title());
            post.setContent(dp.content());
            post.setPostType(dp.type());
            post.setStatus(PostStatus.PUBLISHED);
            post.setTags(dp.tags());
            post.setMediaUrl(dp.mediaUrl());
            post.setThumbnailUrl(dp.thumbnailUrl());
            post.setPortfolioLink(dp.portfolioLink());
            post.setLikeCount(dp.likes());
            post.setCommentCount(dp.comments());
            post.setViewCount(dp.views());
            post.setCreatedAt(LocalDateTime.now().minusHours(dp.hoursAgo()));
            post.setUpdatedAt(post.getCreatedAt());
            post.setTrendingScore(CommunityFeedRanker.computeScore(post));

            savedPosts.add(postRepository.save(post));
        }

        for (int i = 0; i < users.size() - 1; i++) {
            CreatorFollow follow = new CreatorFollow();
            follow.setFollower(users.get(i));
            follow.setFollowing(users.get((i + 1) % users.size()));
            followRepository.save(follow);
        }

        for (CommunityPost post : savedPosts) {
            User liker = users.get((int) (post.getId() % users.size()));
            if (!liker.getId().equals(post.getAuthor().getId())) {
                PostLike like = new PostLike();
                like.setUser(liker);
                like.setPost(post);
                likeRepository.save(like);
            }

            PostComment comment = new PostComment();
            comment.setPost(post);
            comment.setAuthor(users.get((int) ((post.getId() + 1) % users.size())));
            comment.setContent(sampleComment((int) (post.getId() % 5)));
            comment.setCreatedAt(post.getCreatedAt().plusMinutes(30));
            commentRepository.save(comment);
        }

        for (CreatorProfile p : profileRepository.findAll()) {
            long postCount = postRepository.findByStatusAndAuthorIdOrderByCreatedAtDesc(
                    PostStatus.PUBLISHED, p.getUser().getId(),
                    org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements();
            p.setTotalPosts((int) postCount);
            p.setTotalLikes(savedPosts.stream()
                    .filter(post -> post.getAuthor().getId().equals(p.getUser().getId()))
                    .mapToInt(CommunityPost::getLikeCount)
                    .sum());
            profileRepository.save(p);
        }

        log.info("Community demo seed complete: {} creators, {} posts", users.size(), savedPosts.size());
    }

    private static String avatarUrl(String name) {
        String encoded = name.replace(" ", "+");
        return "https://ui-avatars.com/api/?name=" + encoded + "&background=1a1028&color=c9a84c&size=256&bold=true";
    }

    private static List<DemoPost> buildDemoPosts() {
        return List.of(
                new DemoPost("Cinematic wedding film — grade breakdown",
                        "Shared my full DaVinci grade chain for this outdoor wedding. Soft halation + film grain.",
                        PostType.PORTFOLIO, "cinematic, documentaries",
                        "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=1200&q=80",
                        "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=1200&q=80",
                        null, 342, 28, 1200, 4),
                new DemoPost("Before / After — anime fight scene",
                        "2 days rotoscoping + compositing. Glow accents synced to soundtrack hits.",
                        PostType.IMAGE, "anime, motion graphics",
                        "https://images.unsplash.com/photo-1612036789805-4b12b83956c4?w=1200&q=80",
                        "https://images.unsplash.com/photo-1612036789805-4b12b83956c4?w=1200&q=80",
                        null, 518, 41, 2100, 8),
                new DemoPost("Valorant clutch montage — 60s hype cut",
                        "Client wanted fast pacing + bass drops. Used speed ramps + zoom blur transitions.",
                        PostType.VIDEO, "gaming, montage",
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                        "https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&q=80",
                        null, 891, 67, 5400, 12),
                new DemoPost("Brand reel for skincare launch",
                        "Vertical 9:16 delivery. Hook in first 1.2s — retention up 34% vs previous cut.",
                        PostType.VIDEO, "reels, social",
                        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                        "https://images.unsplash.com/photo-1611532736597-de2d90e850f7?w=1200&q=80",
                        "https://reel.demo.hivi/skincare", 445, 33, 3200, 6),
                new DemoPost("Title sequence study — neon noir",
                        "Experimental AE project. Element 3D + deep glow. Feedback welcome.",
                        PostType.PORTFOLIO, "motion graphics, cinematic",
                        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1200&q=80",
                        "https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=1200&q=80",
                        null, 276, 19, 980, 10),
                new DemoPost("[Available] Freelance editor — March slots open",
                        "Taking 2 corporate clients + 1 creator collab. DM for reel + day rate.",
                        PostType.TEXT, "freelance, paid work",
                        null,
                        "https://images.unsplash.com/photo-1598488035139-bdbb2231d1bb?w=1200&q=80",
                        null, 156, 22, 640, 2),
                new DemoPost("Editing tip: speed ramp curves",
                        "Use bezier handles on time remapping — avoids robotic motion. Quick 30s demo inside.",
                        PostType.TEXT, "editing tips, premiere",
                        null,
                        "https://images.unsplash.com/photo-1478720568477-152d9b8e6839?w=1200&q=80",
                        null, 203, 45, 1800, 18),
                new DemoPost("Client showcase — fintech explainer",
                        "90s explainer with motion icons + VO sync. Delivered in 5 business days.",
                        PostType.PORTFOLIO, "corporate, motion graphics",
                        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200&q=80",
                        "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=1200&q=80",
                        "https://portfolio.demo.hivi/fintech", 389, 31, 1500, 14),
                new DemoPost("Travel vlog color — Bali series",
                        "Teal & orange split tone with lifted shadows. LUT pack link in portfolio.",
                        PostType.IMAGE, "cinematic, travel",
                        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&q=80",
                        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&q=80",
                        null, 612, 38, 4100, 20),
                new DemoPost("Hiring: need reel editor for fitness brand",
                        "Paid project — 8 reels/month. Must understand hook retention & captions.",
                        PostType.TEXT, "hiring, paid work, reels",
                        null,
                        "https://images.unsplash.com/photo-1517836357463-d25dfeac3438?w=1200&q=80",
                        null, 98, 14, 420, 1)
        );
    }

    private static String sampleComment(int idx) {
        return switch (idx) {
            case 0 -> "The grade on this is insane — what grain did you use?";
            case 1 -> "This pacing is perfect. Would love a breakdown.";
            case 2 -> "Available for collab? My portfolio is on my profile.";
            case 3 -> "Clean work. Following for more tips.";
            default -> "Incredible edit — shared with my team.";
        };
    }

    private record DemoCreator(
            String username, String displayName, String bio, String niche, String tools,
            String bannerUrl, boolean available
    ) {}

    private record DemoPost(
            String title, String content, PostType type, String tags,
            String mediaUrl, String thumbnailUrl, String portfolioLink,
            int likes, int comments, int views, int hoursAgo
    ) {}
}
