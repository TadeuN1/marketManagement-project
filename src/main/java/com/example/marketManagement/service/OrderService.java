package com.example.marketManagement.service;

import com.example.marketManagement.dto.*;
import com.example.marketManagement.entity.Order;
import com.example.marketManagement.entity.OrderItem;
import com.example.marketManagement.entity.OrderStatus;
import com.example.marketManagement.entity.Product;
import com.example.marketManagement.repository.CustomerRepository;
import com.example.marketManagement.repository.OrderRepository;
import com.example.marketManagement.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository){
    this.orderRepository = orderRepository;
    this.productRepository = productRepository;
    this.customerRepository = customerRepository;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body inválido");
        }
        if (request.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId é obrigatório");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido precisa ter ao menos 1 item");
        }

        // valida cliente
        boolean customerExists = customerRepository.existsById(request.getCustomerId());
        if (!customerExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente não encontrado");
        }

        // cria Order
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setStatus(OrderStatus.NEW);
        order.setCreatedAt(LocalDateTime.now());

        int totalCents = 0;

        for (CreateOrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getProductId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productId é obrigatório");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "quantity deve ser > 0");
            }

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto não encontrado: " + itemReq.getProductId()));

            if (product.getActive() == null || !product.getActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto inativo: " + itemReq.getProductId());
            }

            // preço vem do banco
            int unitPrice = (product.getPriceCents() == null) ? 0 : product.getPriceCents();

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProductId(product.getId());
            oi.setQuantity(itemReq.getQuantity());
            oi.setUnitPriceCents(unitPrice);

            order.getItems().add(oi);

            totalCents += itemReq.getQuantity() * unitPrice;
        }

        Order saved = orderRepository.save(order); // cascade salva itens
        return new CreateOrderResponse(saved.getId(), totalCents);
    }

    private int calculateTotalCents(List<OrderItem> items){
        return items.stream()
                .mapToInt(i -> i.getQuantity() * i.getUnitPriceCents())
                .sum();
    }

    public List<OrderSummaryDTO> listSummaries(){
        return orderRepository.findAll().stream()
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .map(o -> new OrderSummaryDTO(
                    o.getId(),
                        o.getStatus(),
                        o.getCreatedAt(),
                        calculateTotalCents(o.getItems())
        ))
                .toList();
    }

    public OrderDetailDTO getDetail(Integer id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        var safeItems = (order.getItems() == null) ? List.<OrderItem>of() : order.getItems();

        List<OrderItemDTO> items = safeItems.stream()
                .map(i -> new OrderItemDTO(i.getProductId(), i.getQuantity(), i.getUnitPriceCents()))
                .toList();

        int total = items.stream()
                .mapToInt(i -> i.getSubtotalCents() == null ? 0 : i.getSubtotalCents())
                .sum();


        return new OrderDetailDTO(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getCreatedAt(),
                items,
                total
        );
    }
}
