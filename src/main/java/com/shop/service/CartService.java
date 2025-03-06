package com.shop.service;

import com.oauthlogin.api.entity.user.User;
import com.oauthlogin.api.repository.user.UserRepository;
import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Product;

import com.shop.repository.CartRepository;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // 현재 사용자의 장바구니 조회 (없으면 생성)
    public Cart getCartByUsername(String userId) {
        User user = userRepository.findByUserId(userId)
                     .orElseThrow(() -> new RuntimeException("User not found"));
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .cartItems(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }
    
    // 장바구니에 상품 추가 (이미 존재하면 수량 증가)
    public Cart addItemToCart(String username, Long productId, Integer quantity) {
        Cart cart = getCartByUsername(username);
        Product product = productRepository.findById(productId)
                           .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // 동일 상품이 이미 있는지 확인
        List<CartItem> items = cart.getCartItems();
        for (CartItem item : items) {
            if(item.getProduct().getId().equals(productId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return cartRepository.save(cart);
            }
        }
        
        // 새 항목 추가
        CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
        items.add(newItem);
        cart.setCartItems(items);
        return cartRepository.save(cart);
    }
    
    // 장바구니 아이템 수량 업데이트 (수량이 0이면 제거)
    public Cart updateCartItem(String username, Long cartItemId, Integer quantity) {
        Cart cart = getCartByUsername(username);
        List<CartItem> items = cart.getCartItems();
        for (CartItem item : items) {
            if(item.getId().equals(cartItemId)) {
                if(quantity <= 0) {
                    items.remove(item);
                } else {
                    item.setQuantity(quantity);
                }
                break;
            }
        }
        cart.setCartItems(items);
        return cartRepository.save(cart);
    }
    
    // 장바구니 아이템 삭제
    public Cart removeCartItem(String username, Long cartItemId) {
        Cart cart = getCartByUsername(username);
        List<CartItem> items = cart.getCartItems();
        items.removeIf(item -> item.getId().equals(cartItemId));
        cart.setCartItems(items);
        return cartRepository.save(cart);
    }
    
    // 장바구니 비우기 (주문 후 사용)
    public void clearCart(String username) {
        Cart cart = getCartByUsername(username);
        cart.getCartItems().clear();
        cartRepository.save(cart);
    }
}
