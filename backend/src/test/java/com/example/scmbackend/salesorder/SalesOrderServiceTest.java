package com.example.scmbackend.salesorder;

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
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private SalesOrder testSO;
    private Product testProduct;
    private Warehouse testWarehouse;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Wireless Mouse");

        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("Main Warehouse");

        SalesOrderItem testItem = new SalesOrderItem();
        testItem.setId(1L);
        testItem.setProduct(testProduct);
        testItem.setQuantity(30);

        testSO = new SalesOrder();
        testSO.setId(1L);
        testSO.setCustomerName("Rahim Traders");
        testSO.setWarehouse(testWarehouse);
        testSO.setOrderDate(LocalDate.now());
        testSO.setStatus("PENDING");
        testSO.setItems(List.of(testItem));

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProduct(testProduct);
        testInventory.setWarehouse(testWarehouse);
        testInventory.setQuantity(50);
    }

    @Test
    void shipSalesOrder_shouldReduceStock_whenSufficientInventoryExists() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(testSO));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(testInventory));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        salesOrderService.shipSalesOrder(1L);

        assertEquals(20, testInventory.getQuantity()); // 50 - 30
        verify(inventoryRepository).save(testInventory);
    }

    @Test
    void shipSalesOrder_shouldThrowException_whenInsufficientStock() {
        testInventory.setQuantity(10); // less than the requested 30
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(testSO));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(testInventory));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            salesOrderService.shipSalesOrder(1L);
        });

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shipSalesOrder_shouldThrowException_whenNoInventoryRecordExists() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(testSO));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            salesOrderService.shipSalesOrder(1L);
        });

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void shipSalesOrder_shouldThrowException_whenAlreadyShipped() {
        testSO.setStatus("SHIPPED");
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(testSO));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            salesOrderService.shipSalesOrder(1L);
        });

        assertEquals("This sales order was already shipped", exception.getMessage());
    }
}