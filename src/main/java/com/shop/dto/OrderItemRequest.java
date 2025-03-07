package com.shop.dto;
// 주문 요청 시 전달할 DTO
public class OrderItemRequest {
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