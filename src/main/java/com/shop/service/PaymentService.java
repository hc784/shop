package com.shop.service;

import com.shop.dto.PaymentCallbackRequest;
import com.shop.dto.RequestPayDto;
import com.shop.entity.Order;
import com.shop.entity.OrderItem;
import com.shop.entity.PaymentStatus;
import com.shop.repository.OrderRepository;
import com.shop.repository.PaymentRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService{

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final IamportClient iamportClient;

    public RequestPayDto findRequestDto(String orderUid) {
        Order order = orderRepository.findOrderAndPaymentAndMember(orderUid)
                .orElseThrow(() -> new IllegalArgumentException("주문이 없습니다."));

        return RequestPayDto.builder()
                .buyerName(order.getUser().getUsername())
                .buyerEmail(order.getUser().getEmail())
				/* .buyerAddress(order.getMember().getAddress()) */
                .paymentPrice(order.getTotalPrice())
                .itemName(getOrderSummary(order.getOrderItems()))
				.orderUid(order.getOrderUid()) 
                .build();
    }


    public IamportResponse<Payment> paymentByCallback(PaymentCallbackRequest request) {

        try {
            // 결제 단건 조회(아임포트)
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(request.getPaymentUid());
            // 주문내역 조회
            Order order = orderRepository.findOrderAndPayment(request.getOrderUid())
                    .orElseThrow(() -> new IllegalArgumentException("주문 내역이 없습니다."));

            // 결제 완료가 아니면
            if(!iamportResponse.getResponse().getStatus().equals("paid")) {
                // 주문, 결제 삭제
                orderRepository.delete(order);
                paymentRepository.delete(order.getPayment());

                throw new RuntimeException("결제 미완료");
            }

            // DB에 저장된 결제 금액
            int price = order.getTotalPrice();
            // 실 결제 금액
            int iamportPrice = iamportResponse.getResponse().getAmount().intValue();

            // 결제 금액 검증
            if(iamportPrice != price) {
                // 주문, 결제 삭제
                orderRepository.delete(order);
                paymentRepository.delete(order.getPayment());

                // 결제 취소(아임포트)
                iamportClient.cancelPaymentByImpUid(new CancelData(iamportResponse.getResponse().getImpUid(), true, new BigDecimal(iamportPrice)));

                throw new RuntimeException("결제금액 위변조 의심");
            }

            // 결제 상태 변경
            order.getPayment().changePaymentBySuccess(PaymentStatus.OK, iamportResponse.getResponse().getImpUid());

            return iamportResponse;

        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getOrderSummary(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return "주문한 상품이 없습니다.";
        }

        // 첫 번째 상품의 이름 가져오기
        String firstProductName = orderItems.get(0).getProduct().getName();

        // 상품 개수 확인 (첫 번째 상품 외 몇 개가 더 있는지)
        int otherProductCount = orderItems.size() - 1;

        // 상품이 하나만 있으면 그대로 반환
        if (otherProductCount == 0) {
            return firstProductName;
        }

        // 여러 개 있으면 "상품명 외 N개 상품" 형식으로 반환
        return firstProductName + " 외 " + otherProductCount + "개의 상품";
    }

}