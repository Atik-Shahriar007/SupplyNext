package com.example.scmbackend.warehouse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public List<WarehouseResponseDto> getAllWarehouses() {
        return warehouseService.getAllWarehouses();
    }

    @PostMapping
    public WarehouseResponseDto createWarehouse(@Valid @RequestBody WarehouseRequestDto dto) {
        return warehouseService.createWarehouse(dto);
    }
}