package com.example.scmbackend.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardSummaryResponseDto {
    private Double totalInventoryValue;
    private Long totalStockUnits;
    private Integer lowStockThreshold;
    private Integer lowStockItemsCount;
    private List<LowStockItemDto> lowStockItems;
    private Long pendingPurchaseOrders;
    private Long pendingSalesOrders;
    private Long pendingTransfers;
    private Long totalProducts;
    private Long totalWarehouses;
    private Long totalSuppliers;
    private Integer deadStockItemsCount;
    private Double averageSupplierOnTimeRate;
}
