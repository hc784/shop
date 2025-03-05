package com.shop.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.shop.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
}
