package com.example.marketManagement.dto;

import com.example.marketManagement.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDetailDTO {
    private Integer id;
    private Integer customerId;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private List<OrderItemDTO> items;
    private Integer totalCents;

    public OrderDetailDTO(Integer id, Integer customerId, OrderStatus status, LocalDateTime createdAt, List<OrderItemDTO> items, Integer totalCents) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.createdAt = createdAt;
        this.items = items;
        this.totalCents = totalCents;
    }

    public Integer getId() { return id; }
    public Integer getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public List<OrderItemDTO> getItems() { return items; }
    public Integer getTotalCents() { return totalCents; }
}
