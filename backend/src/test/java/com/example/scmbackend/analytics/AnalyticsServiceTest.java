package com.example.scmbackend.analytics;

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
}