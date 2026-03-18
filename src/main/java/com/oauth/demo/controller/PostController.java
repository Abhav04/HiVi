package com.oauth.demo.controller;

import com.oauth.demo.entity.Post;
import com.oauth.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping
    public Post createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication authentication
    ) {

        String username = authentication.getName();

        return postService.createPost(content, file, username);
    }

    @GetMapping
    public List<Post> getAllPosts() {
        return postService.getAllPosts();
    }
}