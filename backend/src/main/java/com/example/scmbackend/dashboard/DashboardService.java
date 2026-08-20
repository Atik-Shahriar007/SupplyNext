package com.example.scmbackend.dashboard;

import com.example.scmbackend.analytics.AnalyticsService;
import com.example.scmbackend.analytics.DeadStockResponseDto;
import com.example.scmbackend.analytics.SupplierAnalyticsResponseDto;
import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.purchaseorder.PurchaseOrderRepository;
import com.example.scmbackend.salesorder.SalesOrderRepository;
import com.example.scmbackend.supplier.SupplierRepository;
import com.example.scmbackend.transfer.TransferRepository;
import com.example.scmbackend.warehouse.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired private InventoryRepository inventoryRepository;
    @Autowired private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired private SalesOrderRepository salesOrderRepository;
    @Autowired private TransferRepository transferRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private WarehouseRepository warehouseRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private AnalyticsService analyticsService;

    private static final int LOW_STOCK_THRESHOLD = 20;

    public DashboardSummaryResponseDto getDashboardSummary() {
        List<Inventory> allInventory = inventoryRepository.findAll();

        double totalInventoryValue = allInventory.stream()
                .mapToDouble(inv -> inv.getQuantity() * inv.getProduct().getPrice())
                .sum();

        long totalStockUnits = allInventory.stream()
                .mapToLong(Inventory::getQuantity)
                .sum();

        List<LowStockItemDto> lowStockItems = allInventory.stream()
                .filter(inv -> inv.getQuantity() < LOW_STOCK_THRESHOLD)
                .map(inv -> new LowStockItemDto(
                        inv.getProduct().getId(),
                        inv.getProduct().getName(),
                        inv.getWarehouse().getId(),
                        inv.getWarehouse().getName(),
                        inv.getQuantity()
                ))
                .collect(Collectors.toList());

        long pendingPurchaseOrders = purchaseOrderRepository.findAll().stream()
                .filter(po -> "PENDING".equals(po.getStatus()))
                .count();

        long pendingSalesOrders = salesOrderRepository.findAll().stream()
                .filter(so -> "PENDING".equals(so.getStatus()))
                .count();

        long pendingTransfers = transferRepository.findAll().stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .count();

        long totalProducts = productRepository.count();
        long totalWarehouses = warehouseRepository.count();
        long totalSuppliers = supplierRepository.count();

        List<DeadStockResponseDto> deadStock = analyticsService.detectDeadStock(90);
        int deadStockItemsCount = (int) deadStock.stream()
                .filter(DeadStockResponseDto::getIsDeadStock)
                .count();

        List<SupplierAnalyticsResponseDto> supplierPerf = analyticsService.calculateSupplierAnalytics();
        List<Double> onTimeRates = supplierPerf.stream()
                .filter(s -> "OK".equals(s.getStatus()))
                .map(SupplierAnalyticsResponseDto::getOnTimeDeliveryRate)
                .collect(Collectors.toList());

        Double averageSupplierOnTimeRate = onTimeRates.isEmpty()
                ? null
                : round2(onTimeRates.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));

        return new DashboardSummaryResponseDto(
                round2(totalInventoryValue), totalStockUnits, LOW_STOCK_THRESHOLD,
                lowStockItems.size(), lowStockItems,
                pendingPurchaseOrders, pendingSalesOrders, pendingTransfers,
                totalProducts, totalWarehouses, totalSuppliers,
                deadStockItemsCount, averageSupplierOnTimeRate
        );
    }

    private Double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
