package com.example.scmbackend.purchaseorder;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderService.getAllPurchaseOrders();
    }

    @PostMapping
    public PurchaseOrder createPurchaseOrder(@Valid @RequestBody PurchaseOrder purchaseOrder) {
        return purchaseOrderService.createPurchaseOrder(purchaseOrder);
    }

    @PatchMapping("/{id}/receive")
    public PurchaseOrder receivePurchaseOrder(@PathVariable Long id) {
        return purchaseOrderService.receivePurchaseOrder(id);
    }
}