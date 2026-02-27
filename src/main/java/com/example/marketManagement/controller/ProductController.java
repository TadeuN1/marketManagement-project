package com.example.marketManagement.controller;

import com.example.marketManagement.entity.Product;
import com.example.marketManagement.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repository;

    public ProductController(ProductRepository repository){
        this.repository = repository;
    }

    @GetMapping
    public List<Product> list(
     @RequestParam(required = false) String name,
     @RequestParam(required = false) String category,
     @RequestParam(required = false) Boolean active
 ){
        if (category != null && active != null) {return repository.findByCategoryAndActive(category, active);}
        if (name != null) {return repository.findByNameContainingIgnoreCase(name);}
        if (category != null) {return repository.findByCategory(category);}
        if (active != null) {return repository.findByActive(active);}
        return repository.findAll();
    }

}
