package com.example.scmbackend.purchaseorder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDto {
    private Long id;

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String productName;

    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
}
