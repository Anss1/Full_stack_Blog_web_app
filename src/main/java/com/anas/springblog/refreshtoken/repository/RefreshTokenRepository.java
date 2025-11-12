package com.anas.springblog.refreshtoken.repository;

import com.anas.springblog.authentecation.model.User;
import com.anas.springblog.refreshtoken.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByToken(String token);
    List<RefreshToken> findAllByUser(User user);
    void deleteAllByUser(User user);
}
