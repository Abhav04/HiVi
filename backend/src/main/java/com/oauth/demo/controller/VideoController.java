package com.oauth.demo.controller;

import com.oauth.demo.entity.Video;
import com.oauth.demo.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/videos")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @PostMapping("/upload")
    public Video uploadVideo(@RequestBody Video video) {
        return videoService.save(video);
    }
    @GetMapping
    public String test() {
        return "Videos API working";
    }
}