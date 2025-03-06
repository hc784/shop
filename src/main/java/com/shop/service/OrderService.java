package com.shop.service;

import com.oauthlogin.api.entity.user.User;
import com.oauthlogin.api.repository.user.UserRepository;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.Product;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    // 주문 생성 (장바구니나 직접 주문 요청 시 사용)
    public Order createOrder(String userId, List<OrderItemRequest> orderItemRequests) {
        User user = userRepository.findByUserId(userId)
                     .orElseThrow(() -> new RuntimeException("User not found"));
        
        Order order = Order.builder()
                        .user(user)
                        .orderDate(LocalDateTime.now())
                        .orderUid(UUID.randomUUID().toString())
                        .status("PROCESSING")
                        .build();
        
        double totalPrice = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for(OrderItemRequest req : orderItemRequests) {
            Product product = productRepository.findById(req.getProductId())
                                .orElseThrow(() -> new RuntimeException("Product not found"));
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(req.getQuantity())
                    .price(product.getPrice())
                    .build();
            orderItems.add(orderItem);
            totalPrice += product.getPrice() * req.getQuantity();
        }
        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);
        
        return orderRepository.save(order);
    }
    
    // 현재 사용자의 모든 주문 조회
    public List<Order> getOrdersByUser(String userId) {
        User user = userRepository.findByUserId(userId)
                     .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user);
    }
    
    // 특정 주문 조회 (현재 사용자 소유 여부 확인)
    public Order getOrderByIdAndUser(String userId, Long orderId) {
        User user = userRepository.findByUserId(userId)
                     .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId)
                      .orElseThrow(() -> new RuntimeException("Order not found"));
        if(!order.getUser().getUserSeq().equals(user.getUserSeq())) {
            throw new RuntimeException("Unauthorized access");
        }
        return order;
    }
    
    // 결제 처리 (포트원 연동)
//    public Order payOrder(String userId, Long orderId) {
//        Order order = getOrderByIdAndUser(userId, orderId);
//        if (!order.getStatus().equals("PROCESSING")) {
//             throw new RuntimeException("Order cannot be paid");
//        }
//        boolean paymentSuccess = paymentService.processPayment(order);
//        if (paymentSuccess) {
//             order.setStatus("COMPLETED");
//             return orderRepository.save(order);
//        } else {
//             throw new RuntimeException("Payment failed");
//        }
//    }
    
    // 주문 요청 시 전달할 DTO
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
        
        public Long getProductId() {
            return productId;
        }
        public void setProductId(Long productId) {
            this.productId = productId;
        }
        public Integer getQuantity() {
            return quantity;
        }
        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }
}
