package com.example.scmbackend.inventory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public Inventory createInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public Inventory adjustStock(Long id, Integer change) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory record not found"));

        int newQuantity = inventory.getQuantity() + change;

        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock: cannot go below 0");
        }

        inventory.setQuantity(newQuantity);
        return inventoryRepository.save(inventory);
    }

    public Optional<Inventory> findByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }
}
