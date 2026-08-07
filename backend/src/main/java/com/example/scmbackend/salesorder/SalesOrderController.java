package com.example.scmbackend.salesorder;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @GetMapping
    public List<SalesOrder> getAllSalesOrders() {
        return salesOrderService.getAllSalesOrders();
    }

    @PostMapping
    public SalesOrder createSalesOrder(@Valid @RequestBody SalesOrder salesOrder) {
        return salesOrderService.createSalesOrder(salesOrder);
    }

    @PatchMapping("/{id}/ship")
    public SalesOrder shipSalesOrder(@PathVariable Long id) {
        return salesOrderService.shipSalesOrder(id);
    }
}