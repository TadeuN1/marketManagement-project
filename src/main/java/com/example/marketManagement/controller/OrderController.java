package com.example.marketManagement.controller;

import com.example.marketManagement.dto.CreateOrderRequest;
import com.example.marketManagement.dto.CreateOrderResponse;
import com.example.marketManagement.dto.OrderDetailDTO;
import com.example.marketManagement.dto.OrderSummaryDTO;
import com.example.marketManagement.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;
    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderSummaryDTO> list() {
        return service.listSummaries();
    }

    @GetMapping("/{id}")
    public OrderDetailDTO detail(@PathVariable Integer id) {
        return service.getDetail(id);
    }

    @PostMapping
    public CreateOrderResponse create(@RequestBody CreateOrderRequest request) {
        return service.createOrder(request);
    }

}