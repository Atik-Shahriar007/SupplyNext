package com.example.scmbackend.purchaseorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class PurchaseOrderRequestDto {
    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<PurchaseOrderItemDto> items;
}
