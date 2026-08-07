package com.example.scmbackend.inventory;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @PostMapping
    public Inventory createInventory(@Valid @RequestBody Inventory inventory) {
        return inventoryService.createInventory(inventory);
    }

    @PatchMapping("/{id}/adjust")
    public Inventory adjustStock(@PathVariable Long id, @RequestBody StockAdjustmentRequest request) {
        return inventoryService.adjustStock(id, request.getChange());
    }
}