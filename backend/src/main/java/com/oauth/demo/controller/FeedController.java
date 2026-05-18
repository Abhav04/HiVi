package com.oauth.demo.controller;

import com.oauth.demo.entity.Post;
import com.oauth.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private PostService postService;

    @GetMapping
    public List<Post> getFeed() {

        return postService.getAllPosts();
    }
}