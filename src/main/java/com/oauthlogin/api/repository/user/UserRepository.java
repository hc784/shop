package com.oauthlogin.api.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oauthlogin.api.entity.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserId(String userId);
    Boolean existsByUserId(String userId);

}
