package com.example.scmbackend.analytics;

import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.salesorder.SalesOrder;
import com.example.scmbackend.salesorder.SalesOrderItem;
import com.example.scmbackend.salesorder.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    private static final double MIN_DAYS_FOR_RELIABLE_DEMAND = 30.0;

    public List<EOQResponseDto> calculateEOQForAllProducts() {
        List<Product> products = productRepository.findAll();
        Map<Long, DemandStats> demandByProduct = computeDemandStats();

        return products.stream()
                .map(product -> buildEOQResponse(product, demandByProduct.get(product.getId())))
                .collect(Collectors.toList());
    }

    public EOQResponseDto calculateEOQForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        DemandStats stats = computeDemandStats().get(productId);
        return buildEOQResponse(product, stats);
    }

    private EOQResponseDto buildEOQResponse(Product product, DemandStats stats) {
        boolean hasCostData = product.getUnitCost() != null
                && product.getHoldingCostRate() != null
                && product.getOrderingCost() != null;

        if (!hasCostData) {
            return EOQResponseDto.missingCostData(product);
        }

        if (stats == null || stats.totalQuantity <= 0) {
            return EOQResponseDto.insufficientData(product);
        }

        double daysSpan = stats.daysSpan();
        double annualDemand = stats.totalQuantity * (365.0 / daysSpan);
        double holdingCostPerUnit = product.getUnitCost() * product.getHoldingCostRate();

        double eoq = Math.sqrt((2 * annualDemand * product.getOrderingCost()) / holdingCostPerUnit);

        String note = daysSpan < MIN_DAYS_FOR_RELIABLE_DEMAND
                ? "Based on limited sales history (" + (int) daysSpan + " days) — treat as a rough estimate."
                : null;

        return EOQResponseDto.ok(product, annualDemand, holdingCostPerUnit, eoq, note);
    }

    private Map<Long, DemandStats> computeDemandStats() {
        List<SalesOrder> shippedOrders = salesOrderRepository.findAll().stream()
                .filter(so -> "SHIPPED".equals(so.getStatus()))
                .collect(Collectors.toList());

        Map<Long, DemandStats> stats = new HashMap<>();

        for (SalesOrder so : shippedOrders) {
            LocalDate date = so.getOrderDate();
            for (SalesOrderItem item : so.getItems()) {
                Long productId = item.getProduct().getId();
                DemandStats s = stats.computeIfAbsent(productId, k -> new DemandStats());
                s.totalQuantity += item.getQuantity();
                if (s.earliestDate == null || date.isBefore(s.earliestDate)) s.earliestDate = date;
                if (s.latestDate == null || date.isAfter(s.latestDate)) s.latestDate = date;
            }
        }
        return stats;
    }

    private static class DemandStats {
        long totalQuantity = 0;
        LocalDate earliestDate;
        LocalDate latestDate;

        double daysSpan() {
            long days = ChronoUnit.DAYS.between(earliestDate, latestDate);
            return Math.max(days, 1.0);
        }
    }
}