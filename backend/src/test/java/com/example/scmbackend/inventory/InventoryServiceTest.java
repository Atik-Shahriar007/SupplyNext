package com.example.scmbackend.inventory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.example.scmbackend.product.Product;
import com.example.scmbackend.warehouse.Warehouse;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        Product testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Wireless Mouse");
        testProduct.setSku("PRD-001");

        Warehouse testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("Main Warehouse");

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProduct(testProduct);
        testInventory.setWarehouse(testWarehouse);
        testInventory.setQuantity(50);
    }

    @Test
    void adjustStock_shouldIncreaseQuantity_whenChangeIsPositive() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponseDto result = inventoryService.adjustStock(1L, 10);

        assertEquals(60, result.getQuantity());
    }

    @Test
    void adjustStock_shouldDecreaseQuantity_whenChangeIsNegative() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InventoryResponseDto result = inventoryService.adjustStock(1L, -20);

        assertEquals(30, result.getQuantity());
    }

    @Test
    void adjustStock_shouldThrowException_whenResultWouldBeNegative() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.adjustStock(1L, -100);
        });

        assertEquals("Insufficient stock: cannot go below 0", exception.getMessage());
    }

    @Test
    void adjustStock_shouldThrowException_whenInventoryNotFound() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            inventoryService.adjustStock(99L, 10);
        });
    }
}