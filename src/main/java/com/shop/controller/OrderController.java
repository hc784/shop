package com.shop.controller;

import com.shop.entity.Order;
import com.shop.service.OrderService;
import com.shop.service.OrderService.OrderItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;
    
    // 주문 생성 (주문 요청 시 OrderItemRequest 목록 전달)
    @PostMapping
    public ResponseEntity<Order> createOrder(Authentication authentication, 
                               @RequestBody List<OrderItemRequest> orderItemRequests) {
        String username = authentication.getName();
        Order order = orderService.createOrder(username, orderItemRequests);
        return ResponseEntity.ok(order);
    }
    
    // 현재 사용자의 모든 주문 내역 조회
    @GetMapping
    public ResponseEntity<List<Order>> getOrders(Authentication authentication) {
        String username = authentication.getName();
        List<Order> orders = orderService.getOrdersByUser(username);
        return ResponseEntity.ok(orders);
    }
    
    // 특정 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(Authentication authentication, @PathVariable Long orderId) {
        String username = authentication.getName();
        Order order = orderService.getOrderByIdAndUser(username, orderId);
        return ResponseEntity.ok(order);
    }
    
    // 결제 처리 (포트원 연동)
    @PostMapping("/{orderId}/pay")
    public ResponseEntity<Order> payOrder(Authentication authentication, @PathVariable Long orderId) {
        String username = authentication.getName();
        Order order = orderService.payOrder(username, orderId);
        return ResponseEntity.ok(order);
    }
}
