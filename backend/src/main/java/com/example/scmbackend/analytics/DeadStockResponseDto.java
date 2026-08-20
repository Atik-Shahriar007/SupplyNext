package com.example.scmbackend.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class DeadStockResponseDto {
    private Long productId;
    private String sku;
    private String productName;
    private Integer totalQuantityOnHand;
    private LocalDate lastSaleDate;      // null if never sold
    private Integer daysSinceLastSale;   // null if never sold
    private Integer thresholdDays;
    private Boolean isDeadStock;
    private String note;
    private List<WarehouseQuantityDto> warehouseStock;
}