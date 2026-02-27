package com.example.marketManagement.dto;

import com.example.marketManagement.entity.OrderStatus;

import java.time.LocalDateTime;

public class OrderSummaryDTO {
    private Integer id;
    private String status;
    private LocalDateTime createdAt;
    private Integer totalCents;

    public OrderSummaryDTO(Integer id, OrderStatus status, LocalDateTime createdAt, Integer totalCents){
        this.id = id;
        this.status = status.name();
        this.createdAt = createdAt;
        this.totalCents = totalCents;
    }

    public Integer getId() {return  id;}
    public String getStatus() {return status;}
    public LocalDateTime getCreatedAt(){return createdAt;}
    public Integer getTotalCents(){return totalCents;}

}
