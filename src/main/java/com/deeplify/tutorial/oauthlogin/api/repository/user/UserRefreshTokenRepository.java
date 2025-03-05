package com.deeplify.tutorial.oauthlogin.api.repository.user;

import com.deeplify.tutorial.oauthlogin.api.entity.user.UserRefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {
    UserRefreshToken findByUserId(String userId);
    UserRefreshToken findByUserIdAndRefreshToken(String userId, String refreshToken);
    @Transactional
    void deleteByRefreshToken(String refreshToken); 
}
