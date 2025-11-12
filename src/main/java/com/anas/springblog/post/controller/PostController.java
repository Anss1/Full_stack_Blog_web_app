package com.anas.springblog.post.controller;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.post.dto.PostRequest;
import com.anas.springblog.post.dto.PostResponse;
import com.anas.springblog.post.service.IPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final IPostService postService;


    @GetMapping
    public Page<PostResponse> getAllPosts(@RequestParam(required = false) String tag, Pageable pageable) {
        return postService.getAllPosts(tag,pageable);
    }
    @GetMapping("/my-posts")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Pageable pageable) {
        Page<PostResponse> posts = postService.getPostByUser(userDetails,pageable);
        return ResponseEntity.status(HttpStatus.OK).body(posts);
    }
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostWithId(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(postService.getPostWithId(id));
    }

    @GetMapping("/search")
    public Page<PostResponse> searchPosts(@RequestParam("keyword") String keyword, Pageable pageable) {
        return postService.searchPosts(keyword, pageable);
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost( @Valid @RequestBody PostRequest postRequest
                                                , @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(postRequest,userDetails));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> editPost(@PathVariable Long id, @Valid @RequestBody PostRequest postRequest,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(postService.editPost(id,postRequest,userDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id
            , @AuthenticationPrincipal CustomUserDetails userDetails) {
         postService.deletePost(id,userDetails);
         return ResponseEntity.noContent().build();
    }
}