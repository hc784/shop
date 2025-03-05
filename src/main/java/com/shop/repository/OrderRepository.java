package com.shop.repository;

import com.oauthlogin.api.entity.user.User;
import com.shop.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
