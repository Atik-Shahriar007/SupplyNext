package com.example.scmbackend.product;

import com.example.scmbackend.category.Category;
import com.example.scmbackend.category.CategoryRepository;
import com.example.scmbackend.supplier.Supplier;
import com.example.scmbackend.supplier.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product createProduct(Product product) {
        Category category = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        product.setCategory(category);
        product.setSupplier(supplier);

        return productRepository.save(product);
    }
}
