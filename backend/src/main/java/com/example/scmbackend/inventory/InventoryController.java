package com.example.scmbackend.inventory;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @PostMapping
    public Inventory createInventory(@Valid @RequestBody Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @PatchMapping("/{id}/adjust")
    public ResponseEntity<?> adjustStock(@PathVariable Long id, @RequestBody StockAdjustmentRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory record not found"));

        int newQuantity = inventory.getQuantity() + request.getChange();

        if (newQuantity < 0) {
            return ResponseEntity.badRequest().body("Insufficient stock: cannot go below 0");
        }

        inventory.setQuantity(newQuantity);
        inventoryRepository.save(inventory);

        return ResponseEntity.ok(inventory);
    }
}