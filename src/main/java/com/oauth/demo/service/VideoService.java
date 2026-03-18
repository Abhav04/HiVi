package com.oauth.demo.service;

import com.oauth.demo.entity.Video;
import com.oauth.demo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    @Autowired
    private VideoRepository videoRepository;

    public Video save(Video video) {
        return videoRepository.save(video);
    }
}