package com.example.marketManagement.controller;

import com.example.marketManagement.dto.CreatePaymentRequest;
import com.example.marketManagement.dto.CreatePaymentResponse;
import com.example.marketManagement.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public CreatePaymentResponse create(@RequestBody CreatePaymentRequest request) {
        return service.register(request);
    }
}
