package com.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.oauthlogin.api.entity.user.User;

@Entity
@Table(name = "orders") // 'order'는 예약어이므로 'orders'
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 주문자 (User 엔티티와 연관)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    private String orderUid; 
    // 주문 날짜
    private LocalDateTime orderDate;
    
    // 총 가격
    private int totalPrice;
    
    // 주문 상태 (PROCESSING, COMPLETED 등)
    private String status;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    
    // 주문 내 여러 상품(주문 아이템)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}
