package com.example.scmbackend.product;

import com.example.scmbackend.category.Category;
import com.example.scmbackend.supplier.Supplier;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @Positive(message = "Price must be greater than 0")
    private Double price;

    // --- New: EOQ cost inputs ---
    private Double unitCost;        // cost to acquire/produce ONE unit (not sale price)
    private Double holdingCostRate; // annual holding cost as a fraction of unitCost, e.g. 0.2 = 20%/year
    private Double orderingCost;    // fixed cost incurred each time a PO is placed for this product


    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
}