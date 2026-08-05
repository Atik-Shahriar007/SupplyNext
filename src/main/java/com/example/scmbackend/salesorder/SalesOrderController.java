package com.example.scmbackend.salesorder;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderRepository.findAll();
    }

    @PostMapping
    public SalesOrder createSalesOrder(@Valid @RequestBody SalesOrder salesOrder) {
        if (salesOrder.getItems() != null) {
            salesOrder.getItems().forEach(item -> item.setSalesOrder(salesOrder));
        }
        salesOrder.setStatus("PENDING");
        return salesOrderRepository.save(salesOrder);
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<?> shipSalesOrder(@PathVariable Long id) {
        SalesOrder so = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        if ("SHIPPED".equals(so.getStatus())) {
            return ResponseEntity.badRequest().body("This sales order was already shipped");
        }

        // First pass: check ALL items have enough stock before changing anything
        for (SalesOrderItem item : so.getItems()) {
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), so.getWarehouse().getId());

            if (inventoryOpt.isEmpty() || inventoryOpt.get().getQuantity() < item.getQuantity()) {
                return ResponseEntity.badRequest().body(
                        "Insufficient stock for product ID " + item.getProduct().getId()
                );
            }
        }

        // Second pass: all checks passed, now actually reduce stock
        for (SalesOrderItem item : so.getItems()) {
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), so.getWarehouse().getId())
                    .orElseThrow();

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
        }

        so.setStatus("SHIPPED");
        salesOrderRepository.save(so);

        return ResponseEntity.ok(so);
    }
}