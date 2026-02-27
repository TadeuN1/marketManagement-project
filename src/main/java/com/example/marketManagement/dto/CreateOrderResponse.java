package com.example.marketManagement.dto;

public class CreateOrderResponse {
    private Integer orderId;
    private Integer totalCents;

    public CreateOrderResponse(Integer orderId, Integer totalCents) {
        this.orderId = orderId;
        this.totalCents = totalCents;
    }

    public Integer getOrderId() { return orderId; }
    public Integer getTotalCents() { return totalCents; }
}
