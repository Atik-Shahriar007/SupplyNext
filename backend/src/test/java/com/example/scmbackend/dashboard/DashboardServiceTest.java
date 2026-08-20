package com.example.scmbackend.dashboard;

import com.example.scmbackend.analytics.AnalyticsService;
import com.example.scmbackend.analytics.DeadStockResponseDto;
import com.example.scmbackend.analytics.SupplierAnalyticsResponseDto;
import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.purchaseorder.PurchaseOrder;
import com.example.scmbackend.purchaseorder.PurchaseOrderRepository;
import com.example.scmbackend.salesorder.SalesOrder;
import com.example.scmbackend.salesorder.SalesOrderRepository;
import com.example.scmbackend.supplier.SupplierRepository;
import com.example.scmbackend.transfer.Transfer;
import com.example.scmbackend.transfer.TransferRepository;
import com.example.scmbackend.warehouse.Warehouse;
import com.example.scmbackend.warehouse.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @Mock private SalesOrderRepository salesOrderRepository;
    @Mock private TransferRepository transferRepository;
    @Mock private ProductRepository productRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private SupplierRepository supplierRepository;
    @Mock private AnalyticsService analyticsService;

    @InjectMocks
    private DashboardService dashboardService;

    private void stubEmptyDefaults() {
        when(purchaseOrderRepository.findAll()).thenReturn(List.of());
        when(salesOrderRepository.findAll()).thenReturn(List.of());
        when(transferRepository.findAll()).thenReturn(List.of());
        when(productRepository.count()).thenReturn(0L);
        when(warehouseRepository.count()).thenReturn(0L);
        when(supplierRepository.count()).thenReturn(0L);
        when(analyticsService.detectDeadStock(90)).thenReturn(List.of());
        when(analyticsService.calculateSupplierAnalytics()).thenReturn(List.of());
    }

    @Test
    void getDashboardSummary_computesInventoryValueAndLowStockCorrectly() {
        Product p1 = new Product();
        p1.setId(1L); p1.setName("Widget"); p1.setPrice(10.0);

        Warehouse w1 = new Warehouse();
        w1.setId(1L); w1.setName("Main");

        Inventory lowStock = new Inventory();
        lowStock.setProduct(p1); lowStock.setWarehouse(w1); lowStock.setQuantity(5);

        Inventory healthyStock = new Inventory();
        healthyStock.setProduct(p1); healthyStock.setWarehouse(w1); healthyStock.setQuantity(50);

        when(inventoryRepository.findAll()).thenReturn(List.of(lowStock, healthyStock));
        stubEmptyDefaults();

        DashboardSummaryResponseDto result = dashboardService.getDashboardSummary();

        assertEquals(550.0, result.getTotalInventoryValue()); // (5+50)*10
        assertEquals(55L, result.getTotalStockUnits());
        assertEquals(1, result.getLowStockItemsCount());
        assertEquals(5, result.getLowStockItems().get(0).getQuantity());
    }

    @Test
    void getDashboardSummary_countsPendingOrdersAndTransfersCorrectly() {
        PurchaseOrder pendingPO = new PurchaseOrder();
        pendingPO.setStatus("PENDING");
        PurchaseOrder receivedPO = new PurchaseOrder();
        receivedPO.setStatus("RECEIVED");

        SalesOrder pendingSO = new SalesOrder();
        pendingSO.setStatus("PENDING");

        Transfer pendingTransfer = new Transfer();
        pendingTransfer.setStatus("PENDING");
        Transfer completedTransfer = new Transfer();
        completedTransfer.setStatus("COMPLETED");

        when(inventoryRepository.findAll()).thenReturn(List.of());
        when(purchaseOrderRepository.findAll()).thenReturn(List.of(pendingPO, receivedPO));
        when(salesOrderRepository.findAll()).thenReturn(List.of(pendingSO));
        when(transferRepository.findAll()).thenReturn(List.of(pendingTransfer, completedTransfer));
        when(productRepository.count()).thenReturn(0L);
        when(warehouseRepository.count()).thenReturn(0L);
        when(supplierRepository.count()).thenReturn(0L);
        when(analyticsService.detectDeadStock(90)).thenReturn(List.of());
        when(analyticsService.calculateSupplierAnalytics()).thenReturn(List.of());

        DashboardSummaryResponseDto result = dashboardService.getDashboardSummary();

        assertEquals(1L, result.getPendingPurchaseOrders());
        assertEquals(1L, result.getPendingSalesOrders());
        assertEquals(1L, result.getPendingTransfers());
    }

    @Test
    void getDashboardSummary_countsOnlyFlaggedDeadStockItems() {
        DeadStockResponseDto dead = new DeadStockResponseDto(
                1L, "SKU1", "Dead Product", 10, null, null, 90, true, "note", List.of());
        DeadStockResponseDto notDead = new DeadStockResponseDto(
                2L, "SKU2", "Active Product", 10, java.time.LocalDate.now(), 1, 90, false, null, List.of());

        when(inventoryRepository.findAll()).thenReturn(List.of());
        when(purchaseOrderRepository.findAll()).thenReturn(List.of());
        when(salesOrderRepository.findAll()).thenReturn(List.of());
        when(transferRepository.findAll()).thenReturn(List.of());
        when(productRepository.count()).thenReturn(0L);
        when(warehouseRepository.count()).thenReturn(0L);
        when(supplierRepository.count()).thenReturn(0L);
        when(analyticsService.detectDeadStock(90)).thenReturn(List.of(dead, notDead));
        when(analyticsService.calculateSupplierAnalytics()).thenReturn(List.of());

        DashboardSummaryResponseDto result = dashboardService.getDashboardSummary();

        assertEquals(1, result.getDeadStockItemsCount());
    }

    @Test
    void getDashboardSummary_averagesOnlyOkStatusSuppliers_forOnTimeRate() {
        SupplierAnalyticsResponseDto ok1 = new SupplierAnalyticsResponseDto(
                1L, "S1", "OK", null, 5, 2, 2, 0, 0, 4.0, 80.0, 80.0);
        SupplierAnalyticsResponseDto ok2 = new SupplierAnalyticsResponseDto(
                2L, "S2", "OK", null, 5, 2, 2, 0, 0, 4.0, 100.0, 100.0);
        SupplierAnalyticsResponseDto notReceived = new SupplierAnalyticsResponseDto(
                3L, "S3", "NO_RECEIVED_ORDERS", "note", null, 0, 0, 0, 0, null, null, null);

        when(inventoryRepository.findAll()).thenReturn(List.of());
        when(purchaseOrderRepository.findAll()).thenReturn(List.of());
        when(salesOrderRepository.findAll()).thenReturn(List.of());
        when(transferRepository.findAll()).thenReturn(List.of());
        when(productRepository.count()).thenReturn(0L);
        when(warehouseRepository.count()).thenReturn(0L);
        when(supplierRepository.count()).thenReturn(0L);
        when(analyticsService.detectDeadStock(90)).thenReturn(List.of());
        when(analyticsService.calculateSupplierAnalytics())
                .thenReturn(List.of(ok1, ok2, notReceived));

        DashboardSummaryResponseDto result = dashboardService.getDashboardSummary();

        assertEquals(90.0, result.getAverageSupplierOnTimeRate()); // (80+100)/2
    }

    @Test
    void getDashboardSummary_returnsNullOnTimeRate_whenNoSuppliersHaveOkStatus() {
        when(inventoryRepository.findAll()).thenReturn(List.of());
        stubEmptyDefaults();

        DashboardSummaryResponseDto result = dashboardService.getDashboardSummary();

        assertNull(result.getAverageSupplierOnTimeRate());
    }
}