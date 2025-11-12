package com.anas.springblog.post.service;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.exception.ResourceNotFoundException;
import com.anas.springblog.post.dto.PostRequest;
import com.anas.springblog.post.dto.PostResponse;
import com.anas.springblog.post.model.Post;
import com.anas.springblog.post.repository.PostRepository;
import com.anas.springblog.tag.model.Tag;
import com.anas.springblog.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostServiceImp implements IPostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Override
    public Page<PostResponse> getAllPosts(String tag, Pageable pageable) {
        Page<Post> postPage;
        if (tag != null) {
            postPage = postRepository.findByTags_Name(tag,pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }
        return postPage.map(PostResponse::fromEntity);
    }

    @Override
    public Page<PostResponse> getPostByUser(CustomUserDetails userDetails, Pageable pageable) {
        Long userId = userDetails.getUser().getId();
        Page<Post> postPage = postRepository.findByUser_Id(userId,pageable);
        return postPage.map(PostResponse::fromEntity);
    }

    @Override
    public PostResponse getPostWithId(Long id) {
        return PostResponse.fromEntity(postRepository.findById(id).orElseThrow());
    }

    @Override
    public PostResponse createPost(PostRequest postRequest, CustomUserDetails userDetails) {
        Set<Tag> tagSet = new HashSet<>();
        if (postRequest.tags() != null) {
            tagSet = findAndPersistTags(postRequest.tags());
        }
        Post post = Post.builder()
                .title(postRequest.title())
                .content(postRequest.content())
                .user(userDetails.getUser())
                .tags(tagSet)
                .build();
        return PostResponse.fromEntity(postRepository.save(post));
    }

    @Override
    public PostResponse editPost(Long id, PostRequest postRequest, CustomUserDetails userDetails){
        Post post = findAndVerifyPostOwner(id,userDetails);
        post.setTitle(postRequest.title());
        post.setContent(postRequest.content());
        Set<Tag> tagSet = new HashSet<>();
        if (postRequest.tags() != null) {
            tagSet = findAndPersistTags(postRequest.tags());
        }
        post.setTags(tagSet);
        return PostResponse.fromEntity(postRepository.save(post));
    }

    @Override
    public void deletePost(Long id, CustomUserDetails userDetails) {
        Post post = findAndVerifyPostOwner(id,userDetails);
        postRepository.delete(post);
    }

    @Override
    public Page<PostResponse> searchPosts(String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword, pageable);
        return posts.map(PostResponse::fromEntity);
    }

    public Post findAndVerifyPostOwner(Long postId,CustomUserDetails userDetails){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        if(!post.getUser().getId().equals(userDetails.getUser().getId())){
            throw new AccessDeniedException("User not authorized for this post");
        }
        return post;
    }
    public Post findPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        return post;
    }
    private Set<Tag> findAndPersistTags(Set<String> tagNames){
        Set<Tag> tagSet = new HashSet<>();
        for(String tagName : tagNames){
            Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                Tag newTag = new Tag();
                newTag.setName(tagName);
                return tagRepository.save(newTag);
            });
            tagSet.add(tag);
        }
        return tagSet;
    }
}
