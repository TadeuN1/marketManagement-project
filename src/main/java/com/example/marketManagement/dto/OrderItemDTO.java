package com.example.marketManagement.dto;

public class OrderItemDTO {

    private Integer productId;
    private Integer quantity;
    private Integer unitPriceCents;
    private Integer subtotalCents;

    public OrderItemDTO(Integer productId, Integer quantity, Integer unitPriceCents) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPriceCents = unitPriceCents;

        int q = (quantity == null) ? 0 : quantity;
        int p = (unitPriceCents == null) ? 0 : unitPriceCents;
        this.subtotalCents = q * p;
    }

    public Integer getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public Integer getUnitPriceCents() { return unitPriceCents; }

    // IMPORTANT: use este nome
    public Integer getSubtotalCents() { return subtotalCents; }
}
