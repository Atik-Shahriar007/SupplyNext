package com.example.scmbackend.dashboard;

import java.util.stream.Collectors;
import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.purchaseorder.PurchaseOrderRepository;
import com.example.scmbackend.salesorder.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    private static final int LOW_STOCK_THRESHOLD = 20;

    @GetMapping("/summary")
    public Map<String, Object> getDashboardSummary() {
        List<Inventory> allInventory = inventoryRepository.findAll();

        double totalInventoryValue = allInventory.stream()
                .mapToDouble(inv -> inv.getQuantity() * inv.getProduct().getPrice())
                .sum();

        long totalStockUnits = allInventory.stream()
                .mapToLong(Inventory::getQuantity)
                .sum();

        List<Inventory> lowStockItems = allInventory.stream()
                .filter(inv -> inv.getQuantity() < LOW_STOCK_THRESHOLD)
                .toList();

        long pendingPurchaseOrders = purchaseOrderRepository.findAll().stream()
                .filter(po -> "PENDING".equals(po.getStatus()))
                .count();

        long pendingSalesOrders = salesOrderRepository.findAll().stream()
                .filter(so -> "PENDING".equals(so.getStatus()))
                .count();

        return Map.of(
                "totalInventoryValue", totalInventoryValue,
                "totalStockUnits", totalStockUnits,
                "lowStockItemsCount", lowStockItems.size(),
                "lowStockItems", lowStockItems,
                "pendingPurchaseOrders", pendingPurchaseOrders,
                "pendingSalesOrders", pendingSalesOrders
        );
    }
}