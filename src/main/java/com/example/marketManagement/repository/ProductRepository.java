package com.example.marketManagement.repository;

import com.example.marketManagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {


    List<Product> findByCategory(String category);
    List<Product> findByActive(Boolean active);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryAndActive(String category, Boolean active);

}
