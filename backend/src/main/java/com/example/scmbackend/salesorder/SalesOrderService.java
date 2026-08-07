package com.example.scmbackend.salesorder;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalesOrderService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderRepository.findAll();
    }

    public SalesOrder createSalesOrder(SalesOrder salesOrder) {
        if (salesOrder.getItems() != null) {
            salesOrder.getItems().forEach(item -> item.setSalesOrder(salesOrder));
        }
        salesOrder.setStatus("PENDING");
        return salesOrderRepository.save(salesOrder);
    }

    public SalesOrder shipSalesOrder(Long id) {
        SalesOrder so = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        if ("SHIPPED".equals(so.getStatus())) {
            throw new RuntimeException("This sales order was already shipped");
        }

        // First pass: check ALL items have enough stock before changing anything
        for (SalesOrderItem item : so.getItems()) {
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), so.getWarehouse().getId());

            if (inventoryOpt.isEmpty() || inventoryOpt.get().getQuantity() < item.getQuantity()) {
                throw new RuntimeException(
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
        return salesOrderRepository.save(so);
    }
}