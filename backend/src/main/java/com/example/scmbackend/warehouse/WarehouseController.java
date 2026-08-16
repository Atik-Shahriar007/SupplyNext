package com.example.scmbackend.warehouse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseService warehouseService;

    @GetMapping
    public Page<WarehouseResponseDto> getAllWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return warehouseService.getAllWarehouses(PageRequest.of(page, size));
    }

    @PostMapping
    public WarehouseResponseDto createWarehouse(@Valid @RequestBody WarehouseRequestDto dto) {
        return warehouseService.createWarehouse(dto);
    }
}