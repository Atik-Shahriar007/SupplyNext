package com.example.scmbackend.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReorderPointResponseDto {
    private Long productId;
    private String sku;
    private String productName;
    private String status; // "OK", "MISSING_LEAD_TIME", "INSUFFICIENT_DATA"
    private String note;
    private Integer leadTimeDays;
    private Double meanDailyDemand;
    private Double safetyStock;
    private Double reorderPoint;
    private List<WarehouseStockDto> warehouseStock;
}