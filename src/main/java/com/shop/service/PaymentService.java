package com.shop.service;

import com.shop.entity.Order;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {
    // 포트원 API를 연동해 결제를 진행하는 부분 (여기서는 시뮬레이션)
    public boolean processPayment(Order order) {
        // 실제 구현 시 HttpClient, RestTemplate 등을 이용해 포트원 API 호출
        // 예: 주문 정보와 결제 금액 등을 전달하고 결과를 반환
        // 현재는 결제 성공으로 가정
        return true;
    }
}
