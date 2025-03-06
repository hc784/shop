package com.shop.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.shop.entity.OrderItem;

import lombok.Builder;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RequestPayDto {
    private String orderUid;
    private List<OrderItem> orderItems;
    private String buyerName;
    private Long paymentPrice;
    private String buyerEmail;
    private String buyerAddress;

    @Builder
    public RequestPayDto(String orderUid, List<OrderItem> orderItems, String buyerName, Long paymentPrice, String buyerEmail, String buyerAddress) {
        this.orderUid = orderUid;
        this.orderItems = orderItems;
        this.buyerName = buyerName;
        this.paymentPrice = paymentPrice;
        this.buyerEmail = buyerEmail;
        this.buyerAddress = buyerAddress;
    }
}