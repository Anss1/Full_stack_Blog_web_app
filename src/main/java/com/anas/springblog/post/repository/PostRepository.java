package com.anas.springblog.post.repository;

import com.anas.springblog.post.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Override
    @EntityGraph(attributePaths = {"comments", "user"}) // Solves N+1 problem for comments and user
    Page<Post> findAll(Pageable pageable);

    Page<Post> findByUser_Id(Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"comments", "user"})
    Page<Post> findByTags_Name(String tag, Pageable pageable);

    List<Post> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"comments", "user"})
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content,Pageable pageable);
}
