package com.example.scmbackend.purchaseorder;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.supplier.Supplier;
import com.example.scmbackend.supplier.SupplierRepository;
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
class PurchaseOrderServiceTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private PurchaseOrder testPO;
    private Product testProduct;
    private Warehouse testWarehouse;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Wireless Mouse");

        testWarehouse = new Warehouse();
        testWarehouse.setId(1L);
        testWarehouse.setName("Main Warehouse");

        Supplier testSupplier = new Supplier();
        testSupplier.setId(1L);
        testSupplier.setName("ABC Traders");

        PurchaseOrderItem testItem = new PurchaseOrderItem();
        testItem.setId(1L);
        testItem.setProduct(testProduct);
        testItem.setQuantity(20);

        testPO = new PurchaseOrder();
        testPO.setId(1L);
        testPO.setSupplier(testSupplier);
        testPO.setWarehouse(testWarehouse);
        testPO.setOrderDate(LocalDate.now());
        testPO.setStatus("PENDING");
        testPO.setItems(List.of(testItem));
    }

    @Test
    void receivePurchaseOrder_shouldIncreaseExistingInventory_whenRecordExists() {
        Inventory existingInventory = new Inventory();
        existingInventory.setId(1L);
        existingInventory.setProduct(testProduct);
        existingInventory.setWarehouse(testWarehouse);
        existingInventory.setQuantity(50);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPO));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(existingInventory));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        purchaseOrderService.receivePurchaseOrder(1L);

        assertEquals(70, existingInventory.getQuantity()); // 50 + 20
        verify(inventoryRepository).save(existingInventory);
    }

    @Test
    void receivePurchaseOrder_shouldCreateNewInventory_whenNoRecordExists() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPO));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.empty());
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        purchaseOrderService.receivePurchaseOrder(1L);

        verify(inventoryRepository).save(argThat(inventory ->
                inventory.getQuantity() == 20 &&
                        inventory.getProduct().getId().equals(1L) &&
                        inventory.getWarehouse().getId().equals(1L)
        ));
    }

    @Test
    void receivePurchaseOrder_shouldThrowException_whenAlreadyReceived() {
        testPO.setStatus("RECEIVED");
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPO));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            purchaseOrderService.receivePurchaseOrder(1L);
        });

        assertEquals("This purchase order was already received", exception.getMessage());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void receivePurchaseOrder_shouldSetStatusToReceived_afterSuccessfulReceive() {
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(testPO));
        when(inventoryRepository.findByProductIdAndWarehouseId(1L, 1L))
                .thenReturn(Optional.of(new Inventory() {{
                    setProduct(testProduct);
                    setWarehouse(testWarehouse);
                    setQuantity(0);
                }}));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrderResponseDto result = purchaseOrderService.receivePurchaseOrder(1L);

        assertEquals("RECEIVED", result.getStatus());
        assertEquals(LocalDate.now(), result.getReceivedDate());
    }
}