package com.example.scmbackend.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDto {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @Positive(message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Unit cost is required")
    @Positive(message = "Unit cost must be greater than 0")
    private Double unitCost;

    @NotNull(message = "Holding cost rate is required")
    @Positive(message = "Holding cost rate must be greater than 0")
    private Double holdingCostRate;

    @NotNull(message = "Ordering cost is required")
    @Positive(message = "Ordering cost must be greater than 0")
    private Double orderingCost;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;
}
