package com.example.scmbackend.purchaseorder;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @GetMapping
    public Page<PurchaseOrderResponseDto> getAllPurchaseOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return purchaseOrderService.getAllPurchaseOrders(PageRequest.of(page, size));
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