package com.example.scmbackend.salesorder;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    @Autowired
    private SalesOrderService salesOrderService;

    @GetMapping
    public Page<SalesOrderResponseDto> getAllSalesOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return salesOrderService.getAllSalesOrders(PageRequest.of(page, size));
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