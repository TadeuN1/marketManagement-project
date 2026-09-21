package com.example.marketManagement.service;

import com.example.marketManagement.dto.CreatePaymentRequest;
import com.example.marketManagement.dto.CreatePaymentResponse;
import com.example.marketManagement.entity.Order;
import com.example.marketManagement.entity.OrderItem;
import com.example.marketManagement.entity.OrderStatus;
import com.example.marketManagement.entity.Payment;
import com.example.marketManagement.repository.OrderRepository;
import com.example.marketManagement.repository.PaymentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    private int calculateOrderTotal(List<OrderItem> items) {
        if (items == null) return 0;
        return items.stream().mapToInt(i -> {
            int q = (i.getQuantity() == null) ? 0 : i.getQuantity();
            int p = (i.getUnitPriceCents() == null) ? 0 : i.getUnitPriceCents();
            return q * p;
        }).sum();
    }

    @Transactional
    public CreatePaymentResponse register(CreatePaymentRequest req) {
        if (req == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid body");
        if (req.getOrderId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderId is required");
        if (req.getMethod() == null || req.getMethod().isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "method is required");
        if (req.getAmountCents() == null || req.getAmountCents() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "amountCents must be >= 0");

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        int orderTotal = calculateOrderTotal(order.getItems());

        // 1) salva pagamento
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setMethod(req.getMethod());
        payment.setAmountCents(req.getAmountCents());
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // 2) soma pagamentos
        int paidTotal = paymentRepository.sumPaidCentsByOrderId(order.getId());

        // 3) atualiza status se cobriu total
        if (paidTotal >= orderTotal && order.getStatus() != OrderStatus.PAID) {
            order.setStatus(OrderStatus.PAID);
        }

        return new CreatePaymentResponse(saved.getId(), order.getId(), orderTotal, paidTotal, order.getStatus());
    }
}
