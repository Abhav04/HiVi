package com.oauth.demo.community.repository;

import com.oauth.demo.community.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);

    List<PostComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByPostId(Long postId);
}
