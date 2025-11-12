package com.anas.springblog.authentecation.service;

import com.anas.springblog.authentecation.dto.UserProfileRequest;
import com.anas.springblog.authentecation.model.UserProfile;

public interface IUserProfileService {
    UserProfile getProfileByUsername(String username);

    UserProfile updateProfile(String username, UserProfileRequest request);
}
