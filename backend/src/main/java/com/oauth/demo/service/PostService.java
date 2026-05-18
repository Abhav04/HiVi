package com.oauth.demo.service;

import com.oauth.demo.entity.Post;
import com.oauth.demo.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class PostService {

    @Autowired
    private PostRepository postRepository;
    @CacheEvict(value = "posts", allEntries = true)
    public Post createPost(String content, MultipartFile file, String username) {
        Post post = new Post();
        post.setContent(content);
        post.setUsername(username);
        post.setCreatedAt(LocalDateTime.now());

        // handle file if uploaded
        if (file != null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            post.setMediaUrl(fileName);
        }

        return postRepository.save(post);
    }
    @Cacheable("posts")
    public List<Post> getAllPosts() {
        System.out.println("🔥 FETCHING FROM DATABASE");
        return postRepository.findAll();
    }
}