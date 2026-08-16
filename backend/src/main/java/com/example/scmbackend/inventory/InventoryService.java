package com.example.scmbackend.inventory;

import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.warehouse.Warehouse;
import com.example.scmbackend.warehouse.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    public Page<InventoryResponseDto> getAllInventory(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(this::toResponseDto);
    }

    public InventoryResponseDto createInventory(InventoryRequestDto dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQuantity(dto.getQuantity());

        Inventory saved = inventoryRepository.save(inventory);
        return toResponseDto(saved);
    }

    public InventoryResponseDto adjustStock(Long id, Integer change) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory record not found"));

        int newQuantity = inventory.getQuantity() + change;

        if (newQuantity < 0) {
            throw new RuntimeException("Insufficient stock: cannot go below 0");
        }

        inventory.setQuantity(newQuantity);
        Inventory saved = inventoryRepository.save(inventory);
        return toResponseDto(saved);
    }

    public Optional<Inventory> findByProductAndWarehouse(Long productId, Long warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
    }

    private InventoryResponseDto toResponseDto(Inventory inventory) {
        return new InventoryResponseDto(
                inventory.getId(),
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getProduct().getSku(),
                inventory.getWarehouse().getId(),
                inventory.getWarehouse().getName(),
                inventory.getQuantity()
        );
    }
}
