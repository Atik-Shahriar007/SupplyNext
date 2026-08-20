package com.example.scmbackend.product;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private Double price;
    private Double unitCost;
    private Double holdingCostRate;
    private Double orderingCost;
    private Long categoryId;
    private String categoryName;
    private Long supplierId;
    private String supplierName;
}
