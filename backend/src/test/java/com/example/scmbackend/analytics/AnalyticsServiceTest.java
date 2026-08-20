package com.example.scmbackend.analytics;

import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.salesorder.SalesOrder;
import com.example.scmbackend.salesorder.SalesOrderItem;
import com.example.scmbackend.salesorder.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.scmbackend.supplier.Supplier;
import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.warehouse.Warehouse;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private Product productWithCostData(Long id) {
        Product p = new Product();
        p.setId(id);
        p.setSku("PRD-00" + id);
        p.setName("Test Product " + id);
        p.setUnitCost(10.0);
        p.setHoldingCostRate(0.2); // 20%/year -> holding cost per unit = $2/year
        p.setOrderingCost(50.0);
        return p;
    }

    private SalesOrderItem itemFor(Product product, int quantity) {
        SalesOrderItem item = new SalesOrderItem();
        item.setProduct(product);
        item.setQuantity(quantity);
        return item;
    }

    @Test
    void calculateEOQ_returnsMissingCostData_whenCostFieldsAreNull() {
        Product product = new Product();
        product.setId(1L);
        product.setSku("PRD-001");
        product.setName("No Cost Data");
        // unitCost/holdingCostRate/orderingCost left null

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(List.of());

        EOQResponseDto result = analyticsService.calculateEOQForProduct(1L);

        assertEquals("MISSING_COST_DATA", result.getStatus());
        assertNull(result.getEoq());
    }

    @Test
    void calculateEOQ_returnsInsufficientData_whenNoShippedOrders() {
        Product product = productWithCostData(1L);

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(List.of());

        EOQResponseDto result = analyticsService.calculateEOQForProduct(1L);

        assertEquals("INSUFFICIENT_DATA", result.getStatus());
        assertNull(result.getEoq());
    }

    @Test
    void calculateEOQ_computesExpectedValue_givenKnownDemandAndCosts() {
        Product product = productWithCostData(1L);

        SalesOrder order1 = new SalesOrder();
        order1.setStatus("SHIPPED");
        order1.setOrderDate(LocalDate.of(2026, 1, 1));
        order1.setItems(List.of(itemFor(product, 100)));

        SalesOrder order2 = new SalesOrder();
        order2.setStatus("SHIPPED");
        order2.setOrderDate(LocalDate.of(2026, 7, 1)); // 181 days after order1
        order2.setItems(List.of(itemFor(product, 100)));

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(List.of(order1, order2));

        EOQResponseDto result = analyticsService.calculateEOQForProduct(1L);

        assertEquals("OK", result.getStatus());
        // totalQuantity=200 over 181 days -> annualDemand = 200 * 365/181 ≈ 403.31
        assertEquals(403.31, result.getAnnualDemand(), 0.5);
        // holdingCostPerUnit = 10 * 0.2 = 2.0
        assertEquals(2.0, result.getHoldingCostPerUnit());
        // eoq = sqrt(2 * 403.31 * 50 / 2) ≈ 142.0
        assertTrue(result.getEoq() > 100 && result.getEoq() < 180);
    }

    @Test
    void calculateEOQ_ignoresPendingOrders_whenComputingDemand() {
        Product product = productWithCostData(1L);

        SalesOrder pendingOrder = new SalesOrder();
        pendingOrder.setStatus("PENDING");
        pendingOrder.setOrderDate(LocalDate.of(2026, 1, 1));
        pendingOrder.setItems(List.of(itemFor(product, 999)));

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(List.of(pendingOrder));

        EOQResponseDto result = analyticsService.calculateEOQForProduct(1L);

        assertEquals("INSUFFICIENT_DATA", result.getStatus());
    }

    @Test
    void calculateABCAnalysis_classifiesProductsIntoTiers() {
        Product productA = new Product();
        productA.setId(1L); productA.setSku("A"); productA.setName("Product A"); productA.setUnitCost(7.0);

        Product productB = new Product();
        productB.setId(2L); productB.setSku("B"); productB.setName("Product B"); productB.setUnitCost(2.0);

        Product productC = new Product();
        productC.setId(3L); productC.setSku("C"); productC.setName("Product C"); productC.setUnitCost(1.0);

        LocalDate d1 = LocalDate.of(2025, 1, 1);
        LocalDate d2 = d1.plusDays(365); // exactly 1 year span -> annualDemand == totalQuantity

        List<SalesOrder> orders = new java.util.ArrayList<>();
        for (Product p : List.of(productA, productB, productC)) {
            SalesOrder o1 = new SalesOrder();
            o1.setStatus("SHIPPED"); o1.setOrderDate(d1);
            o1.setItems(List.of(itemFor(p, 50)));

            SalesOrder o2 = new SalesOrder();
            o2.setStatus("SHIPPED"); o2.setOrderDate(d2);
            o2.setItems(List.of(itemFor(p, 50)));

            orders.add(o1);
            orders.add(o2);
        }
        // demand = 100 for each -> values: A=700, B=200, C=100, total=1000
        // cumulative: A=70% -> A tier, B=90% -> B tier, C=100% -> C tier

        when(productRepository.findAll()).thenReturn(List.of(productA, productB, productC));
        when(salesOrderRepository.findAll()).thenReturn(orders);

        List<ABCAnalysisResponseDto> results = analyticsService.calculateABCAnalysis();
        var byId = results.stream()
                .collect(java.util.stream.Collectors.toMap(ABCAnalysisResponseDto::getProductId, r -> r));

        assertEquals("A", byId.get(1L).getTier());
        assertEquals("B", byId.get(2L).getTier());
        assertEquals("C", byId.get(3L).getTier());
    }

    @Test
    void calculateABCAnalysis_flagsMissingCostDataSeparately() {
        Product noCostProduct = new Product();
        noCostProduct.setId(9L); noCostProduct.setSku("X"); noCostProduct.setName("No Cost Product");
        // unitCost left null deliberately

        when(productRepository.findAll()).thenReturn(List.of(noCostProduct));
        when(salesOrderRepository.findAll()).thenReturn(List.of());

        List<ABCAnalysisResponseDto> results = analyticsService.calculateABCAnalysis();

        assertEquals(1, results.size());
        assertEquals("MISSING_COST_DATA", results.get(0).getStatus());
        assertNull(results.get(0).getTier());
    }

    @Test
    void calculateABCAnalysis_handlesZeroTotalValueWithoutDivideByZero() {
        Product p = new Product();
        p.setId(1L); p.setSku("Z"); p.setName("Zero Demand"); p.setUnitCost(10.0);

        when(productRepository.findAll()).thenReturn(List.of(p));
        when(salesOrderRepository.findAll()).thenReturn(List.of());

        List<ABCAnalysisResponseDto> results = analyticsService.calculateABCAnalysis();

        assertEquals("OK", results.get(0).getStatus());
        assertEquals(0.0, results.get(0).getAnnualConsumptionValue());
        assertEquals("C", results.get(0).getTier());
    }
    @Test
    void calculateSafetyStock_returnsMissingLeadTime_whenSupplierLeadTimeIsNull() {
        Supplier supplier = new Supplier();
        supplier.setId(1L); supplier.setName("Supplier A"); // leadTimeDays left null

        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1"); product.setSupplier(supplier);

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(List.of());

        SafetyStockResponseDto result = analyticsService.calculateSafetyStockForProduct(1L, 0.95);

        assertEquals("MISSING_LEAD_TIME", result.getStatus());
        assertNull(result.getSafetyStock());
    }

    @Test
    void calculateSafetyStock_returnsInsufficientData_whenDemandSpansLessThanTwoDays() {
        Supplier supplier = new Supplier();
        supplier.setId(1L); supplier.setName("Supplier A"); supplier.setLeadTimeDays(9);

        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1"); product.setSupplier(supplier);

        SalesOrder order = new SalesOrder();
        order.setStatus("SHIPPED"); order.setOrderDate(LocalDate.of(2026, 1, 1));
        order.setItems(List.of(itemFor(product, 10)));

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(List.of(order));

        SafetyStockResponseDto result = analyticsService.calculateSafetyStockForProduct(1L, 0.95);

        assertEquals("INSUFFICIENT_DATA", result.getStatus());
    }

    @Test
    void calculateSafetyStock_computesExpectedValue_givenKnownDailyDemandVariation() {
        Supplier supplier = new Supplier();
        supplier.setId(1L); supplier.setName("Supplier A"); supplier.setLeadTimeDays(9);

        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1"); product.setSupplier(supplier);

        LocalDate base = LocalDate.of(2026, 1, 1);
        int[] quantities = {10, 20, 10, 20};
        List<SalesOrder> orders = new java.util.ArrayList<>();
        for (int i = 0; i < quantities.length; i++) {
            SalesOrder o = new SalesOrder();
            o.setStatus("SHIPPED");
            o.setOrderDate(base.plusDays(i));
            o.setItems(List.of(itemFor(product, quantities[i])));
            orders.add(o);
        }

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(orders);

        SafetyStockResponseDto result = analyticsService.calculateSafetyStockForProduct(1L, 0.95);

        assertEquals("OK", result.getStatus());
        assertEquals(15.0, result.getMeanDailyDemand(), 0.01);
        assertEquals(5.77, result.getStdDevDailyDemand(), 0.05);
        // Z(1.645) * stdDev(5.7735) * sqrt(leadTime=9=>3) ≈ 28.5
        assertEquals(28.5, result.getSafetyStock(), 0.5);
    }

    @Test
    void calculateSafetyStock_throwsException_forUnsupportedServiceLevel() {
        assertThrows(RuntimeException.class, () ->
                analyticsService.calculateSafetyStockForProduct(1L, 0.5));
    }

    @Test
    void calculateReorderPoint_computesExpectedValue_andFlagsWarehousesBelowThreshold() {
        Supplier supplier = new Supplier();
        supplier.setId(1L); supplier.setName("Supplier A"); supplier.setLeadTimeDays(9);

        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1"); product.setSupplier(supplier);

        LocalDate base = LocalDate.of(2026, 1, 1);
        int[] quantities = {10, 20, 10, 20};
        List<SalesOrder> orders = new java.util.ArrayList<>();
        for (int i = 0; i < quantities.length; i++) {
            SalesOrder o = new SalesOrder();
            o.setStatus("SHIPPED");
            o.setOrderDate(base.plusDays(i));
            o.setItems(List.of(itemFor(product, quantities[i])));
            orders.add(o);
        }

        Warehouse lowStockWarehouse = new Warehouse();
        lowStockWarehouse.setId(1L); lowStockWarehouse.setName("Low Stock WH");

        Warehouse highStockWarehouse = new Warehouse();
        highStockWarehouse.setId(2L); highStockWarehouse.setName("High Stock WH");

        Inventory lowInv = new Inventory();
        lowInv.setProduct(product); lowInv.setWarehouse(lowStockWarehouse); lowInv.setQuantity(100);

        Inventory highInv = new Inventory();
        highInv.setProduct(product); highInv.setWarehouse(highStockWarehouse); highInv.setQuantity(200);

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(orders);
        when(inventoryRepository.findAll()).thenReturn(List.of(lowInv, highInv));

        ReorderPointResponseDto result = analyticsService.calculateReorderPointForProduct(1L, 0.95);

        assertEquals("OK", result.getStatus());
        // meanDailyDemand=15, leadTime=9 -> 135, + safetyStock≈28.5 -> ≈163.5
        assertEquals(163.5, result.getReorderPoint(), 1.0);

        var byWarehouse = result.getWarehouseStock().stream()
                .collect(java.util.stream.Collectors.toMap(WarehouseStockDto::getWarehouseId, w -> w));

        assertTrue(byWarehouse.get(1L).getBelowReorderPoint());  // 100 <= ~163.5
        assertFalse(byWarehouse.get(2L).getBelowReorderPoint()); // 200 > ~163.5
    }

    @Test
    void calculateReorderPoint_leavesWarehouseFlagsNull_whenReorderPointNotComputable() {
        Supplier supplier = new Supplier();
        supplier.setId(1L); supplier.setName("Supplier A"); // no leadTimeDays

        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1"); product.setSupplier(supplier);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L); warehouse.setName("WH");

        Inventory inv = new Inventory();
        inv.setProduct(product); inv.setWarehouse(warehouse); inv.setQuantity(50);

        when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product));
        when(salesOrderRepository.findAll()).thenReturn(List.of());
        when(inventoryRepository.findAll()).thenReturn(List.of(inv));

        ReorderPointResponseDto result = analyticsService.calculateReorderPointForProduct(1L, 0.95);

        assertEquals("MISSING_LEAD_TIME", result.getStatus());
        assertNull(result.getReorderPoint());
        assertNull(result.getWarehouseStock().get(0).getBelowReorderPoint());
        assertEquals(50, result.getWarehouseStock().get(0).getCurrentQuantity());
    }

    @Test
    void detectDeadStock_flagsProductWithNoSalesHistory_asDeadStock() {
        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L); warehouse.setName("WH");

        Inventory inv = new Inventory();
        inv.setProduct(product); inv.setWarehouse(warehouse); inv.setQuantity(20);

        when(inventoryRepository.findAll()).thenReturn(List.of(inv));
        when(salesOrderRepository.findAll()).thenReturn(List.of());

        List<DeadStockResponseDto> results = analyticsService.detectDeadStock(90);

        assertEquals(1, results.size());
        assertTrue(results.get(0).getIsDeadStock());
        assertNull(results.get(0).getLastSaleDate());
    }

    @Test
    void detectDeadStock_doesNotFlagRecentlySoldProduct() {
        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L); warehouse.setName("WH");

        Inventory inv = new Inventory();
        inv.setProduct(product); inv.setWarehouse(warehouse); inv.setQuantity(20);

        SalesOrder recentOrder = new SalesOrder();
        recentOrder.setStatus("SHIPPED");
        recentOrder.setOrderDate(LocalDate.now().minusDays(10));
        recentOrder.setItems(List.of(itemFor(product, 5)));

        when(inventoryRepository.findAll()).thenReturn(List.of(inv));
        when(salesOrderRepository.findAll()).thenReturn(List.of(recentOrder));

        List<DeadStockResponseDto> results = analyticsService.detectDeadStock(90);

        assertFalse(results.get(0).getIsDeadStock());
        assertEquals(10, results.get(0).getDaysSinceLastSale());
    }

    @Test
    void detectDeadStock_flagsProductWithOldLastSale_pastThreshold() {
        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L); warehouse.setName("WH");

        Inventory inv = new Inventory();
        inv.setProduct(product); inv.setWarehouse(warehouse); inv.setQuantity(20);

        SalesOrder oldOrder = new SalesOrder();
        oldOrder.setStatus("SHIPPED");
        oldOrder.setOrderDate(LocalDate.now().minusDays(120));
        oldOrder.setItems(List.of(itemFor(product, 5)));

        when(inventoryRepository.findAll()).thenReturn(List.of(inv));
        when(salesOrderRepository.findAll()).thenReturn(List.of(oldOrder));

        List<DeadStockResponseDto> results = analyticsService.detectDeadStock(90);

        assertTrue(results.get(0).getIsDeadStock());
        assertEquals(120, results.get(0).getDaysSinceLastSale());
    }

    @Test
    void detectDeadStock_excludesProductsWithZeroTotalStock() {
        Product product = new Product();
        product.setId(1L); product.setSku("P1"); product.setName("Product 1");

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L); warehouse.setName("WH");

        Inventory inv = new Inventory();
        inv.setProduct(product); inv.setWarehouse(warehouse); inv.setQuantity(0);

        when(inventoryRepository.findAll()).thenReturn(List.of(inv));
        when(salesOrderRepository.findAll()).thenReturn(List.of());

        List<DeadStockResponseDto> results = analyticsService.detectDeadStock(90);

        assertTrue(results.isEmpty());
    }
}