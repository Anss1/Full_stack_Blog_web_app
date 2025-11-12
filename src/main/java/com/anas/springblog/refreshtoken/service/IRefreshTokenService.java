package com.anas.springblog.refreshtoken.service;

import com.anas.springblog.authentecation.model.User;
import com.anas.springblog.refreshtoken.model.RefreshToken;
import org.springframework.transaction.annotation.Transactional;

public interface IRefreshTokenService {
    @Transactional
    String createToken(User user);

    @Transactional
    String createToken(String oldToken, User user);

    RefreshToken verifyAndRetrieveToken(String token);

    @Transactional
    void revokeToken(String token);

    @Transactional
    void deleteByUser(User user);

    User getUserFromRefreshToken(String refreshToken);
}
