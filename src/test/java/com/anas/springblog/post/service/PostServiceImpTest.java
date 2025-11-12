package com.anas.springblog.post.service;

import com.anas.springblog.authentecation.model.CustomUserDetails;
import com.anas.springblog.authentecation.model.Role;
import com.anas.springblog.authentecation.model.User;
import com.anas.springblog.post.dto.PostRequest;
import com.anas.springblog.post.dto.PostResponse;
import com.anas.springblog.post.model.Post;
import com.anas.springblog.post.repository.PostRepository;
import com.anas.springblog.tag.model.Tag;
import com.anas.springblog.tag.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceImpTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private TagRepository tagRepository;
    @InjectMocks
    private PostServiceImp postService;

    private User testUser;
    private Post testPost;
    private Pageable pageable;

    // initializer
    @BeforeEach
    void setup(){
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .role(Role.USER)
                .build();
        testPost = Post.builder()
                .id(1L)
                .title("Test Post Title")
                .content("Test Post Content")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .tags(Collections.emptySet())
                .build();
        pageable = PageRequest.of(0,10);
    }

    // Arrange - Act - Assert (AAA)
    @Test
    @DisplayName("Should return all posts when no tag is provided")
    void shouldReturnAllPostsWhenNoTagIsProvided(){
        //Arrange
        Page<Post> postPage = new PageImpl<>(List.of(testPost));
        when(postRepository.findAll(pageable)).thenReturn(postPage);

        //Act
        Page<PostResponse> result = postService.getAllPosts(null,pageable);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo(testPost.getTitle());
        assertThat(result.getContent().getFirst().authorName()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("Should return all posts related to this tag when provided")
    void shouldReturnAllPostsRelatedToThisTagWhenProvided(){
        //Arrange
        String java = "java";
        Tag tag = new Tag(1L,java);
        Post taggedPost = Post.builder()
                .id(1L)
                .title("Test Post Title")
                .content("Test Post Content")
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .tags(Set.of(tag))
                .build();
        Page<Post> postPage = new PageImpl<>(List.of(taggedPost));
        when(postRepository.findByTags_Name(java,pageable)).thenReturn(postPage);

        //Act
        Page<PostResponse> result = postService.getAllPosts(java,pageable);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().title()).isEqualTo(taggedPost.getTitle());
        assertThat(result.getContent().getFirst().authorName()).isEqualTo(testUser.getUsername());
        assertThat(result.getContent().getFirst().tags()).containsExactlyInAnyOrder(java);
    }

    @Test
    @DisplayName("Should return an empty page when no posts are found for a given tag")
    void shouldReturnEmptyPageWhenNoPostsFoundForTag(){
        //Arrange
        String nonExist = "nonexist";
        Page<Post> emptyPage = Page.empty(pageable);
        when(postRepository.findByTags_Name(nonExist,pageable)).thenReturn(emptyPage);

        //Act
        Page<PostResponse> result = postService.getAllPosts(nonExist,pageable);

        //Assert
        assertThat(result).isNotNull();
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should create a post successfully with new tags")
    void shouldCreatePostSuccessfullyWithNewTags(){
        String newTagName1 = "spring";
        String newTagName2 = "java";
        PostRequest request = new PostRequest(
                "New Post Title",
                "New Post Content",
                Set.of(newTagName1, newTagName2)
        );
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        Tag newTag1 = new Tag(2L,newTagName1);
        Tag newTag2 = new Tag(3L,newTagName2);

        when(tagRepository.findByName(newTagName1)).thenReturn(Optional.empty());
        when(tagRepository.findByName(newTagName2)).thenReturn(Optional.empty());

        when(tagRepository.save(ArgumentMatchers.any(Tag.class)))
                .thenAnswer(invocation -> {
                    Tag tagToSave = invocation.getArgument(0);
                    if (tagToSave.getName().equals(newTagName1)){
                        tagToSave.setId(newTag1.getId());
                        return tagToSave;
                    } else if (tagToSave.getName().equals(newTagName2)) {
                        tagToSave.setId(newTag2.getId());
                        return tagToSave;
                    }
                    return tagToSave;
                });
        Post savedPost = Post.builder()
                .id(1L)
                .title(request.title())
                .content(request.content())
                .user(testUser)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .tags(Set.of(newTag1,newTag2))
                .build();
        when(postRepository.save(ArgumentMatchers.any(Post.class))).thenReturn(savedPost);

        // Act
        PostResponse result = postService.createPost(request, userDetails);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(savedPost.getId());
        assertThat(result.title()).isEqualTo(request.title());
        assertThat(result.content()).isEqualTo(request.content());
        assertThat(result.authorName()).isEqualTo(testUser.getUsername());
        assertThat(result.tags()).containsExactlyInAnyOrder(newTagName1, newTagName2);

        Mockito.verify(tagRepository).findByName(newTagName1);
        Mockito.verify(tagRepository).findByName(newTagName2);
        Mockito.verify(tagRepository, Mockito.times(2))
                .save(ArgumentMatchers.any(Tag.class));

        Mockito.verify(postRepository).save(ArgumentMatchers.any(Post.class));
    }

    @Test
    @DisplayName("Should edit post successfully when user is the owner")
    void shouldEditPostSuccessfullyWhenUserIsOwner(){
        // Arrange
        String newContent = "new content";
        String newTitle = "new title";
        Set<String> newTags = Set.of("java","spring");

        PostRequest postRequest = new PostRequest(newTitle,newContent,newTags);

        Tag existingTag = new Tag(1L, "java");
        Tag newTag = new Tag(2L, "spring");

        // 1. When searching for "java", we return the existing tag.
        when(tagRepository.findByName("java")).thenReturn(Optional.of(existingTag));
        // 2. When searching for "spring", we return empty tag.
        when(tagRepository.findByName("spring")).thenReturn(Optional.empty());
        // 3. Then save and return the new tag.
        when(tagRepository.save(ArgumentMatchers.any(Tag.class))).thenReturn(newTag);

        CustomUserDetails customUserDetails = new CustomUserDetails(testUser);

        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));

        Post updatedPost = Post.builder()
                .id(testPost.getId())
                .title(postRequest.title())
                .content(postRequest.content())
                .tags(Set.of(existingTag,newTag))
                .user(testUser)
                .build();

        when(postRepository.save(ArgumentMatchers.any(Post.class))).thenReturn(updatedPost);

        // Act
        PostResponse response = postService.editPost(testPost.getId(),postRequest,customUserDetails);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(testPost.getId());
        assertThat(response.title()).isEqualTo(postRequest.title());
        assertThat(response.content()).isEqualTo(postRequest.content());
        assertThat(response.tags()).containsExactlyInAnyOrder("spring","java");

    }
}
