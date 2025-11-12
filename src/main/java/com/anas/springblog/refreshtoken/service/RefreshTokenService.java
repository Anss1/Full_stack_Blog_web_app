package com.anas.springblog.refreshtoken.service;

import com.anas.springblog.authentecation.model.User;
import com.anas.springblog.exception.InvalidRequestException;
import com.anas.springblog.exception.ResourceNotFoundException;
import com.anas.springblog.refreshtoken.model.RefreshToken;
import com.anas.springblog.refreshtoken.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements IRefreshTokenService {

    @Value("${app.security.jwt.refresh-token-expiration-ms}")
    private Long refreshTokenExpirationMs; // 7 days = 1000 * 60 * 60 * 24 * 7
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    @Override
    public String createToken(User user) {
        String generatedToken = generateToken();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(generatedToken)
                .expiryDate(setExpiration())
                .user(user)
                .build();
        refreshTokenRepository.save(newRefreshToken);
        return generatedToken;
    }

    @Transactional
    @Override
    public String createToken(String oldToken, User user) {
        String generatedToken = generateToken();
        RefreshToken newRefreshToken = RefreshToken.builder()
                .token(generatedToken)
                .expiryDate(setExpiration())
                .user(user)
                .build();
        rotateToken(oldToken);
        refreshTokenRepository.save(newRefreshToken);
        return generatedToken;
    }

    @Override
    public RefreshToken verifyAndRetrieveToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        // check if the token revoked from a normal logout
        if (refreshToken.isRevoked()) {
            throw new InvalidRequestException("token revoked");
        }
        // check if the token is used before
        if (refreshToken.isUsed()) {
            // suspect of theft so logout user everywhere by mark all tokens for this user as used = true
            logoutEverywhere(refreshToken.getUser());
            throw new InvalidRequestException("token already used before");
        }
        if (isExpired(refreshToken.getExpiryDate())) {
            throw new RuntimeException("token expired");
        }
        return refreshToken;
    }

    @Transactional
    @Override
    public void revokeToken(String token) {
        RefreshToken tokenObject = verifyAndRetrieveToken(token);
        tokenObject.setRevoked(true);
        refreshTokenRepository.save(tokenObject);
    }

    @Transactional
    @Override
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    @Override
    public User getUserFromRefreshToken(String refreshToken) {
        RefreshToken tokenObject = verifyAndRetrieveToken(refreshToken);
        return tokenObject.getUser();
    }

    private String generateToken() {
        // byte array with size 32 byte to store the generated Secure Pseudo Random Number we can make it 64 byte too
        byte[] randomKey = new byte[32];
        // generate (CSPRNG) (eg. [12, -5, 22, -88, ...])
        new SecureRandom().nextBytes(randomKey);
        // encode the generated raw binary bytes to string so we can store it in database and use in http requests/responses
        // the getUrlEncoder is to replace characters like (+, /) with (-,_) so make it Urls friendly
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomKey);
    }

    @Transactional
    private void rotateToken(String token) {
        RefreshToken tokenObject = verifyAndRetrieveToken(token);
        tokenObject.setUsed(true);
        refreshTokenRepository.save(tokenObject);
    }

    @Transactional
    private void logoutEverywhere(User user) {
        List<RefreshToken> tokenList = refreshTokenRepository.findAllByUser(user);
        tokenList.forEach(token ->
        {
            token.setUsed(true);
            refreshTokenRepository.save(token);
        });
    }

    private Instant setExpiration() {
        return Instant.now().plusMillis(refreshTokenExpirationMs);
    }

    private boolean isExpired(Instant expiration) {
        return expiration.isBefore(Instant.now());
    }

    private RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("token not found"));
    }

}
