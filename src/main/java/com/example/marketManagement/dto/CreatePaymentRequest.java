package com.example.marketManagement.dto;

public class CreatePaymentRequest {
    private Integer orderId;
    private String method;
    private Integer amountCents;

    public Integer getOrderId() {return orderId; }
    public void setOrderId(Integer orderId) {this.orderId = orderId;}

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Integer getAmountCents() { return amountCents; }
    public void setAmountCents(Integer amountCents) { this.amountCents = amountCents; }

}
