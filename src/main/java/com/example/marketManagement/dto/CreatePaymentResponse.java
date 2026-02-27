package com.example.marketManagement.dto;

import com.example.marketManagement.entity.OrderStatus;

public class CreatePaymentResponse {
    private Integer paymentId;
    private Integer orderId;
    private Integer orderTotalCents;
    private Integer paidTotalCents;
    private String orderStatus;

    public CreatePaymentResponse(Integer paymentId, Integer orderId, Integer orderTotalCents, Integer paidTotalCents, OrderStatus orderStatus) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.orderTotalCents = orderTotalCents;
        this.paidTotalCents = paidTotalCents;
        this.orderStatus = orderStatus.name();
    }

    public Integer getPaymentId() { return paymentId; }
    public Integer getOrderId() { return orderId; }
    public Integer getOrderTotalCents() { return orderTotalCents; }
    public Integer getPaidTotalCents() { return paidTotalCents; }
    public String getOrderStatus() { return orderStatus; }
}
