package com.example.scmbackend.inventory;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public Page<InventoryResponseDto> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return inventoryService.getAllInventory(PageRequest.of(page, size));
    }

    @PostMapping
    public InventoryResponseDto createInventory(@Valid @RequestBody InventoryRequestDto dto) {
        return inventoryService.createInventory(dto);
    }

    @PatchMapping("/{id}/adjust")
    public InventoryResponseDto adjustStock(@PathVariable Long id, @RequestBody StockAdjustmentRequest request) {
        return inventoryService.adjustStock(id, request.getChange());
    }
}