package com.example.scmbackend.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WarehouseStockDto {
    private Long warehouseId;
    private String warehouseName;
    private Integer currentQuantity;
    private Boolean belowReorderPoint; // null when reorder point isn't computable
}