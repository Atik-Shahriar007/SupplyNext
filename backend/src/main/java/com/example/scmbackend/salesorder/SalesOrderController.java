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
    public List<SalesOrderResponseDto> getAllSalesOrders() {
        return salesOrderService.getAllSalesOrders();
    }

    @PostMapping
    public SalesOrderResponseDto createSalesOrder(@Valid @RequestBody SalesOrderRequestDto dto) {
        return salesOrderService.createSalesOrder(dto);
    }

    @PatchMapping("/{id}/ship")
    public SalesOrderResponseDto shipSalesOrder(@PathVariable Long id) {
        return salesOrderService.shipSalesOrder(id);
    }
}