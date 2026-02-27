package com.example.marketManagement.dto;

import java.util.List;

public class CreateOrderRequest {

    private Integer customerId;
    private List<CreateOrderItemRequest> items;

    public Integer getCustomerId(){return customerId;}
    public void setCustomerId(Integer customerId) {this.customerId = customerId;}

    public List<CreateOrderItemRequest> getItems() {return items;}
    public void setItems(List<CreateOrderItemRequest> items) {this.items = items;}
}
