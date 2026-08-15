package com.example.scmbackend.salesorder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SalesOrderRequestDto {
    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotNull(message = "Warehouse ID is required")
    private Long warehouseId;

    @NotNull(message = "Order date is required")
    private LocalDate orderDate;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<SalesOrderItemDto> items;
}
