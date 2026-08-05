package com.example.scmbackend.purchaseorder;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @GetMapping
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    @PostMapping
    public PurchaseOrder createPurchaseOrder(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        // Link each item back to its parent PO (required for the relationship to save correctly)
        if (purchaseOrder.getItems() != null) {
            purchaseOrder.getItems().forEach(item -> item.setPurchaseOrder(purchaseOrder));
        }
        purchaseOrder.setStatus("PENDING");
        return purchaseOrderRepository.save(purchaseOrder);
    }
}
