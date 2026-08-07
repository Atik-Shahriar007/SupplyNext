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
    public List<PurchaseOrderResponseDto> getAllPurchaseOrders() {
        return purchaseOrderService.getAllPurchaseOrders();
    }

    @PostMapping
    public PurchaseOrderResponseDto createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequestDto dto) {
        return purchaseOrderService.createPurchaseOrder(dto);
    }

    @PatchMapping("/{id}/receive")
    public PurchaseOrderResponseDto receivePurchaseOrder(@PathVariable Long id) {
        return purchaseOrderService.receivePurchaseOrder(id);
    }
}