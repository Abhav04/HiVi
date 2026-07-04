package com.oauth.demo.community.repository;

import com.oauth.demo.community.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdAndParentIsNullOrderByCreatedAtDesc(Long postId);

    List<PostComment> findByParentIdOrderByCreatedAtAsc(Long parentId);

    long countByPostId(Long postId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM PostComment c WHERE c.post.id = :postId AND c.parent IS NOT NULL")
    void deleteByPostIdAndParentIdIsNotNull(@org.springframework.data.repository.query.Param("postId") Long postId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("DELETE FROM PostComment c WHERE c.post.id = :postId AND c.parent IS NULL")
    void deleteByPostIdAndParentIdIsNull(@org.springframework.data.repository.query.Param("postId") Long postId);
}
