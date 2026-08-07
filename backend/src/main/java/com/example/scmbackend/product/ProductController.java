package com.example.scmbackend.product;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductResponseDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping
    public ProductResponseDto createProduct(@Valid @RequestBody ProductRequestDto dto) {
        return productService.createProduct(dto);
    }
}