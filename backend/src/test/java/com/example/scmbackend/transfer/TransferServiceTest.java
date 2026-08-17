package com.example.scmbackend.transfer;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.warehouse.Warehouse;
import com.example.scmbackend.warehouse.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TransferService transferService;

    private Transfer testTransfer;
    private Product testProduct;
    private Warehouse fromWarehouse;
    private Warehouse toWarehouse;
    private Inventory sourceInventory;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Wireless Mouse");

        fromWarehouse = new Warehouse();
        fromWarehouse.setId(1L);
        fromWarehouse.setName("Main Warehouse");

        toWarehouse = new Warehouse();
        toWarehouse.setId(2L);
        toWarehouse.setName("Secondary Warehouse");

        TransferItem testItem = new TransferItem();
        testItem.setId(1L);
        testItem.setProduct(testProduct);
        testItem.setQuantity(15);

        testTransfer = new Transfer();
        testTransfer.setId(1L);
        testTransfer.setFromWarehouse(fromWarehouse);
        testTransfer.setToWarehouse(toWarehouse);
        testTransfer.setTransferDate(LocalDate.now());
        testTransfer.setStatus("PENDING");
        testTransfer.setItems(List.of(testItem));

        sourceInventory = new Inventory();
        sourceInventory.setId(1L);
        sourceInventory.setProduct(testProduct);
        sourceInventory.setWarehouse(fromWarehouse);
        sourceInventory.setQuantity(50);
    }

    @Test
    void completeTransfer_shouldMoveStock_whenSufficientInventoryExists() {
        when(transferRepository.findById(1L)).thenReturn(Optional.of(testTransfer));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 2L))
                .thenReturn(Optional.empty());
        when(transferRepository.save(any(Transfer.class))).thenAnswer(inv -> inv.getArgument(0));

        transferService.completeTransfer(1L);

        assertEquals(35, sourceInventory.getQuantity()); // 50 - 15
        verify(inventoryRepository).save(sourceInventory);
        verify(inventoryRepository).save(argThat(inventory ->
                inventory.getWarehouse().getId().equals(2L) &&
                        inventory.getQuantity() == 15
        ));
    }

    @Test
    void completeTransfer_shouldThrowException_whenInsufficientSourceStock() {
        sourceInventory.setQuantity(5); // less than requested 15
        when(transferRepository.findById(1L)).thenReturn(Optional.of(testTransfer));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(sourceInventory));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferService.completeTransfer(1L);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void completeTransfer_shouldThrowException_whenAlreadyCompleted() {
        testTransfer.setStatus("COMPLETED");
        when(transferRepository.findById(1L)).thenReturn(Optional.of(testTransfer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferService.completeTransfer(1L);
        });

        assertEquals("This transfer was already completed", exception.getMessage());
    }
}