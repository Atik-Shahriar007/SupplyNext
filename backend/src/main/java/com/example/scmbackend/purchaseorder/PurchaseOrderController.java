package com.example.scmbackend.purchaseorder;

import java.util.Optional;
import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    @PostMapping
    public PurchaseOrder createPurchaseOrder(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getItems() != null) {
            purchaseOrder.getItems().forEach(item -> item.setPurchaseOrder(purchaseOrder));
        }
        purchaseOrder.setStatus("PENDING");
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @PatchMapping("/{id}/receive")
    public ResponseEntity<?> receivePurchaseOrder(@PathVariable Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if ("RECEIVED".equals(po.getStatus())) {
            return ResponseEntity.badRequest().body("This purchase order was already received");
        }

        // For each item in the PO, find or create the matching Inventory record and increase stock
        for (PurchaseOrderItem item : po.getItems()) {
            Optional<Inventory> existingInventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), po.getWarehouse().getId());

            if (existingInventory.isPresent()) {
                Inventory inventory = existingInventory.get();
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                inventoryRepository.save(inventory);
            } else {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(item.getProduct());
                newInventory.setWarehouse(po.getWarehouse());
                newInventory.setQuantity(item.getQuantity());
                inventoryRepository.save(newInventory);
            }
        }  // <-- this closing brace for the for-loop was missing

        po.setStatus("RECEIVED");
        purchaseOrderRepository.save(po);

        return ResponseEntity.ok(po);
    }
}
