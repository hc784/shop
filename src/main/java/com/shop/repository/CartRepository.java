package com.shop.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.oauthlogin.api.entity.user.User;
import com.shop.entity.Cart;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
}
