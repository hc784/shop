package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;
    
    // 현재 사용자의 장바구니 조회
    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        String username = authentication.getName();
        Cart cart = cartService.getCartByUsername(username);
        return ResponseEntity.ok(cart);
    }
    
    // 장바구니에 상품 추가
    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(Authentication authentication,
                                          @RequestParam Long productId,
                                          @RequestParam Integer quantity) {
        String username = authentication.getName();
        Cart cart = cartService.addItemToCart(username, productId, quantity);
        return ResponseEntity.ok(cart);
    }
    
    // 장바구니 아이템 수량 업데이트
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<Cart> updateCartItem(Authentication authentication,
                                                 @PathVariable Long cartItemId,
                                                 @RequestParam Integer quantity) {
        String username = authentication.getName();
        Cart cart = cartService.updateCartItem(username, cartItemId, quantity);
        return ResponseEntity.ok(cart);
    }
    
    // 장바구니 아이템 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Cart> removeCartItem(Authentication authentication,
                                                 @PathVariable Long cartItemId) {
        String username = authentication.getName();
        Cart cart = cartService.removeCartItem(username, cartItemId);
        return ResponseEntity.ok(cart);
    }
    
    // 장바구니 비우기 (주문 후 호출)
    @DeleteMapping
    public ResponseEntity<String> clearCart(Authentication authentication) {
        String username = authentication.getName();
        cartService.clearCart(username);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}
