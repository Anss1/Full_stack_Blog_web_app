package com.anas.springblog.authentecation.service;

import com.anas.springblog.authentecation.dto.AuthRequest;
import com.anas.springblog.authentecation.dto.AuthResponse;
import com.anas.springblog.authentecation.dto.RegistrationRequest;
import com.anas.springblog.authentecation.model.User;

public interface IUserService {
    User loadUserByUsername(String username);

    User loadUserByID(Long userId);

    User registerUser(RegistrationRequest request);

    AuthResponse authenticateUser(AuthRequest request);

    void logout(String refreshToken);

    AuthResponse refreshAccessToken(String refreshToken);
}
