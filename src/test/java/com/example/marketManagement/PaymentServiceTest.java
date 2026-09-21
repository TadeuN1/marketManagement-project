package com.example.marketManagement;

import com.example.marketManagement.dto.CreatePaymentRequest;
import com.example.marketManagement.dto.CreatePaymentResponse;
import com.example.marketManagement.entity.Order;
import com.example.marketManagement.entity.OrderItem;
import com.example.marketManagement.entity.OrderStatus;
import com.example.marketManagement.entity.Payment;
import com.example.marketManagement.repository.OrderRepository;
import com.example.marketManagement.repository.PaymentRepository;
import com.example.marketManagement.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Order openOrder() {
        OrderItem item = new OrderItem();
        item.setProductId(1);
        item.setQuantity(2);
        item.setUnitPriceCents(4990);
        Order order = new Order();
        order.setId(1);
        order.setCustomerId(1);
        order.setStatus(OrderStatus.NEW);
        order.setItems(List.of(item));
        return order;
    }

    private CreatePaymentRequest paymentRequest(Integer orderId, String method, int amountCents) {
        CreatePaymentRequest req = new CreatePaymentRequest();
        req.setOrderId(orderId);
        req.setMethod(method);
        req.setAmountCents(amountCents);
        return req;
    }

    @Test
    void fullPaymentFlipsOrderToPaid() {
        Order order = openOrder();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(7);
            return p;
        });
        when(paymentRepository.sumPaidCentsByOrderId(1)).thenReturn(9980);

        CreatePaymentResponse res = paymentService.register(paymentRequest(1, "PIX", 9980));

        assertEquals("PAID", res.getOrderStatus());
        assertEquals(9980, res.getPaidTotalCents());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void partialPaymentKeepsOrderOpen() {
        Order order = openOrder();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentRepository.sumPaidCentsByOrderId(1)).thenReturn(5000);

        CreatePaymentResponse res = paymentService.register(paymentRequest(1, "PIX", 5000));

        assertEquals("NEW", res.getOrderStatus());
        assertEquals(OrderStatus.NEW, order.getStatus());
    }

    @Test
    void paymentForMissingOrderIsNotFound() {
        when(orderRepository.findById(9999)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.register(paymentRequest(9999, "PIX", 100)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void blankMethodIsRejected() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> paymentService.register(paymentRequest(1, "  ", 100)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(paymentRepository, never()).save(any());
    }
}
