package com.anas.springblog.authentication.service;

import com.anas.springblog.authentecation.dto.UserProfileRequest;
import com.anas.springblog.authentecation.model.User;
import com.anas.springblog.authentecation.model.UserProfile;
import com.anas.springblog.authentecation.repository.UserProfileRepository;
import com.anas.springblog.authentecation.service.IUserService;
import com.anas.springblog.authentecation.service.UserProfileServiceImp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceImpTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private IUserService userService;

    @InjectMocks
    private UserProfileServiceImp userProfileService;

    @Test
    void getProfileByUsername_whenUserExists_shouldReturnUserProfile() {
        UserProfile mockProfile = new UserProfile();
        mockProfile.setId(1L);
        mockProfile.setBio("Test bio");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setUserProfile(mockProfile);
        mockProfile.setUser(mockUser);

        when(userService.loadUserByUsername("testUser")).thenReturn(mockUser);

        UserProfile result = userProfileService.getProfileByUsername("testUser");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBio()).isEqualTo("Test bio");
        assertThat(result.getUser().getUsername()).isEqualTo("testUser");

        verify(userService).loadUserByUsername("testUser");
    }

    @Test
    void updateProfileByUsername_whenUserExists_shouldUpdateThenReturnUserProfile() {
        UserProfile mockProfile = new UserProfile();
        mockProfile.setId(1L);
        mockProfile.setBio("old bio");
        mockProfile.setWebsite("old-website.com");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        mockUser.setUserProfile(mockProfile);
        mockProfile.setUser(mockUser);

        UserProfileRequest request = new UserProfileRequest("new bio", "new-website.com");

        UserProfile updatedProfile = new UserProfile();
        updatedProfile.setId(1L);
        updatedProfile.setBio("new bio");
        updatedProfile.setWebsite("new-website.com");

        when(userService.loadUserByUsername("testUser")).thenReturn(mockUser);
        when(userProfileRepository.save(any(UserProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserProfile result = userProfileService.updateProfile("testUser", request);

        ArgumentCaptor<UserProfile> userProfileArgumentCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(userProfileArgumentCaptor.capture());
        UserProfile capturedProfile = userProfileArgumentCaptor.getValue();

        assertThat(capturedProfile.getBio()).isEqualTo("new bio");
        assertThat(capturedProfile.getWebsite()).isEqualTo("new-website.com");

        assertThat(result.getBio()).isEqualTo("new bio");
    }

    @Test
    void getProfileByUsername_whenUserDoesNotExist_shouldThrowException() {
        when(userService.loadUserByUsername("usernotexist"))
                .thenThrow(new UsernameNotFoundException("user with username usernotexist not found"));

        UsernameNotFoundException exception =
                Assertions.assertThrows(UsernameNotFoundException.class,
                        () -> userProfileService.getProfileByUsername("usernotexist"));

        assertThat(exception.getMessage()).isEqualTo("user with username usernotexist not found");
        verify(userService).loadUserByUsername("usernotexist");
    }
}
