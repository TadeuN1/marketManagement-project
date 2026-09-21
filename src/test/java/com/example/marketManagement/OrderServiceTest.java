package com.example.marketManagement;

import com.example.marketManagement.dto.CreateOrderItemRequest;
import com.example.marketManagement.dto.CreateOrderRequest;
import com.example.marketManagement.dto.CreateOrderResponse;
import com.example.marketManagement.entity.Order;
import com.example.marketManagement.entity.Product;
import com.example.marketManagement.repository.CustomerRepository;
import com.example.marketManagement.repository.OrderRepository;
import com.example.marketManagement.repository.ProductRepository;
import com.example.marketManagement.service.OrderService;
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
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Product product(Integer id, int priceCents, boolean active) {
        Product p = new Product();
        p.setId(id);
        p.setName("Camiseta");
        p.setPriceCents(priceCents);
        p.setActive(active);
        return p;
    }

    private CreateOrderRequest request(Integer customerId, Integer productId, int quantity) {
        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(customerId);
        req.setItems(List.of(item));
        return req;
    }

    @Test
    void createsOrderUsingDatabasePricesForTotal() {
        when(customerRepository.existsById(1)).thenReturn(true);
        when(productRepository.findById(1)).thenReturn(Optional.of(product(1, 4990, true)));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(12);
            return o;
        });

        CreateOrderResponse res = orderService.createOrder(request(1, 1, 2));

        assertEquals(12, res.getOrderId());
        assertEquals(9980, res.getTotalCents());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void rejectsInactiveProduct() {
        when(customerRepository.existsById(1)).thenReturn(true);
        when(productRepository.findById(4)).thenReturn(Optional.of(product(4, 25990, false)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(request(1, 4, 1)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownCustomer() {
        when(customerRepository.existsById(99)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(request(99, 1, 1)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void rejectsEmptyItems() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setCustomerId(1);
        req.setItems(List.of());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(req));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void rejectsZeroQuantity() {
        when(customerRepository.existsById(1)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.createOrder(request(1, 1, 0)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void detailOfMissingOrderIsNotFound() {
        when(orderRepository.findById(9999)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> orderService.getDetail(9999));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
