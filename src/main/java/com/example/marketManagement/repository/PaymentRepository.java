package com.example.marketManagement.repository;

import com.example.marketManagement.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    @Query("select coalesce(sum(p.amountCents), 0) from Payment p where p.orderId = :orderId")
    int sumPaidCentsByOrderId(@Param("orderId") Integer orderId);
}
