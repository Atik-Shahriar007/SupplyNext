package com.example.scmbackend.analytics;

import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.salesorder.SalesOrder;
import com.example.scmbackend.salesorder.SalesOrderItem;
import com.example.scmbackend.salesorder.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.supplier.Supplier;
import com.example.scmbackend.supplier.SupplierRepository;
import com.example.scmbackend.purchaseorder.PurchaseOrder;
import com.example.scmbackend.purchaseorder.PurchaseOrderRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;


@Service
public class AnalyticsService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

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


    public List<ABCAnalysisResponseDto> calculateABCAnalysis() {
        List<Product> products = productRepository.findAll();
        Map<Long, DemandStats> demandByProduct = computeDemandStats();

        List<ABCAnalysisResponseDto> missingCostResults = new java.util.ArrayList<>();
        List<ProductValue> valued = new java.util.ArrayList<>();

        for (Product p : products) {
            if (p.getUnitCost() == null) {
                missingCostResults.add(ABCAnalysisResponseDto.missingCostData(p));
                continue;
            }

            DemandStats stats = demandByProduct.get(p.getId());
            double annualDemand = (stats == null || stats.totalQuantity <= 0)
                    ? 0.0
                    : stats.totalQuantity * (365.0 / stats.daysSpan());

            double value = annualDemand * p.getUnitCost();
            valued.add(new ProductValue(p, annualDemand, value));
        }

        valued.sort((a, b) -> Double.compare(b.value, a.value));

        double totalValue = valued.stream().mapToDouble(v -> v.value).sum();

        List<ABCAnalysisResponseDto> results = new java.util.ArrayList<>();
        double cumulativeValue = 0.0;

        for (ProductValue pv : valued) {
            cumulativeValue += pv.value;
            double percentOfTotal = totalValue > 0 ? (pv.value / totalValue) * 100.0 : 0.0;
            double cumulativePercent = totalValue > 0 ? (cumulativeValue / totalValue) * 100.0 : 0.0;

            String tier;
            if (totalValue <= 0) {
                tier = "C";
            } else if (cumulativePercent <= 80.0) {
                tier = "A";
            } else if (cumulativePercent <= 95.0) {
                tier = "B";
            } else {
                tier = "C";
            }

            results.add(ABCAnalysisResponseDto.ok(pv.product, pv.annualDemand, pv.value,
                    percentOfTotal, cumulativePercent, tier));
        }

        results.addAll(missingCostResults);
        return results;
    }

    private static class ProductValue {
        Product product;
        double annualDemand;
        double value;

        ProductValue(Product product, double annualDemand, double value) {
            this.product = product;
            this.annualDemand = annualDemand;
            this.value = value;
        }
    }


    // --- Shared helper ---
    private List<SalesOrder> getShippedSalesOrders() {
        return salesOrderRepository.findAll().stream()
                .filter(so -> "SHIPPED".equals(so.getStatus()))
                .collect(Collectors.toList());
    }



    private Map<Long, DemandStats> computeDemandStats() {
        List<SalesOrder> shippedOrders = getShippedSalesOrders();

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

    // --- Safety Stock methods
    private static final Map<Double, Double> Z_SCORES = Map.of(
            0.90, 1.28,
            0.95, 1.645,
            0.975, 1.96,
            0.99, 2.33,
            0.999, 3.09
    );

    public List<SafetyStockResponseDto> calculateSafetyStock(double serviceLevel) {
        double z = zScoreFor(serviceLevel);
        List<Product> products = productRepository.findAll();
        Map<Long, DailyDemandStats> demandStats = computeDailyDemandStats();

        return products.stream()
                .map(p -> buildSafetyStockResponse(p, demandStats.get(p.getId()), serviceLevel, z))
                .collect(Collectors.toList());
    }

    public SafetyStockResponseDto calculateSafetyStockForProduct(Long productId, double serviceLevel) {
        double z = zScoreFor(serviceLevel);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        DailyDemandStats stats = computeDailyDemandStats().get(productId);
        return buildSafetyStockResponse(product, stats, serviceLevel, z);
    }

    private double zScoreFor(double serviceLevel) {
        Double z = Z_SCORES.get(serviceLevel);
        if (z == null) {
            throw new RuntimeException("Unsupported service level: " + serviceLevel +
                    ". Supported values: " + Z_SCORES.keySet());
        }
        return z;
    }

    private SafetyStockResponseDto buildSafetyStockResponse(Product product, DailyDemandStats stats,
                                                            double serviceLevel, double z) {
        Integer leadTimeDays = product.getSupplier().getLeadTimeDays();

        if (leadTimeDays == null) {
            return SafetyStockResponseDto.missingLeadTime(product, serviceLevel, z);
        }

        if (stats == null || !stats.sufficient) {
            return SafetyStockResponseDto.insufficientData(product, serviceLevel, z, leadTimeDays);
        }

        double safetyStock = z * stats.stdDevDailyDemand * Math.sqrt(leadTimeDays);

        String note = stats.spanDays < 30
                ? "Based on limited sales history (" + stats.spanDays + " days) — treat as a rough estimate."
                : null;

        return SafetyStockResponseDto.ok(product, serviceLevel, z, leadTimeDays,
                stats.meanDailyDemand, stats.stdDevDailyDemand, safetyStock, note);
    }

    private Map<Long, DailyDemandStats> computeDailyDemandStats() {
        List<SalesOrder> shippedOrders = getShippedSalesOrders();

        Map<Long, Map<LocalDate, Long>> dailyQtyByProduct = new HashMap<>();

        for (SalesOrder so : shippedOrders) {
            LocalDate date = so.getOrderDate();
            for (SalesOrderItem item : so.getItems()) {
                Long productId = item.getProduct().getId();
                dailyQtyByProduct
                        .computeIfAbsent(productId, k -> new HashMap<>())
                        .merge(date, (long) item.getQuantity(), Long::sum);
            }
        }

        Map<Long, DailyDemandStats> result = new HashMap<>();
        for (Map.Entry<Long, Map<LocalDate, Long>> entry : dailyQtyByProduct.entrySet()) {
            Map<LocalDate, Long> byDate = entry.getValue();
            LocalDate min = Collections.min(byDate.keySet());
            LocalDate max = Collections.max(byDate.keySet());
            long spanDays = ChronoUnit.DAYS.between(min, max) + 1;

            if (spanDays < 2) {
                result.put(entry.getKey(), DailyDemandStats.insufficient());
                continue;
            }

            double[] series = new double[(int) spanDays];
            LocalDate cursor = min;
            int i = 0;
            while (!cursor.isAfter(max)) {
                series[i++] = byDate.getOrDefault(cursor, 0L);
                cursor = cursor.plusDays(1);
            }

            double mean = Arrays.stream(series).average().orElse(0.0);
            double sumSqDiff = Arrays.stream(series).map(v -> (v - mean) * (v - mean)).sum();
            double sampleVariance = sumSqDiff / (series.length - 1);
            double stdDev = Math.sqrt(sampleVariance);

            result.put(entry.getKey(), new DailyDemandStats(mean, stdDev, true, (int) spanDays));
        }

        return result;
    }

    private static class DailyDemandStats {
        double meanDailyDemand;
        double stdDevDailyDemand;
        boolean sufficient;
        int spanDays;

        DailyDemandStats(double mean, double stdDev, boolean sufficient, int spanDays) {
            this.meanDailyDemand = mean;
            this.stdDevDailyDemand = stdDev;
            this.sufficient = sufficient;
            this.spanDays = spanDays;
        }

        static DailyDemandStats insufficient() {
            return new DailyDemandStats(0, 0, false, 0);
        }
    }

    //re-order method
    public List<ReorderPointResponseDto> calculateReorderPoints(double serviceLevel) {
        double z = zScoreFor(serviceLevel);
        List<Product> products = productRepository.findAll();
        Map<Long, DailyDemandStats> demandStats = computeDailyDemandStats();
        List<Inventory> allInventory = inventoryRepository.findAll();

        return products.stream()
                .map(p -> buildReorderPointResponse(p, demandStats.get(p.getId()), serviceLevel, z, allInventory))
                .collect(Collectors.toList());
    }

    public ReorderPointResponseDto calculateReorderPointForProduct(Long productId, double serviceLevel) {
        double z = zScoreFor(serviceLevel);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
        DailyDemandStats stats = computeDailyDemandStats().get(productId);
        List<Inventory> allInventory = inventoryRepository.findAll();
        return buildReorderPointResponse(product, stats, serviceLevel, z, allInventory);
    }

    private ReorderPointResponseDto buildReorderPointResponse(Product product, DailyDemandStats stats,
                                                              double serviceLevel, double z,
                                                              List<Inventory> allInventory) {
        SafetyStockResponseDto ss = buildSafetyStockResponse(product, stats, serviceLevel, z);

        Double reorderPoint = "OK".equals(ss.getStatus())
                ? ss.getMeanDailyDemand() * ss.getLeadTimeDays() + ss.getSafetyStock()
                : null;

        List<WarehouseStockDto> warehouseStocks = allInventory.stream()
                .filter(inv -> inv.getProduct().getId().equals(product.getId()))
                .map(inv -> new WarehouseStockDto(
                        inv.getWarehouse().getId(),
                        inv.getWarehouse().getName(),
                        inv.getQuantity(),
                        reorderPoint != null ? inv.getQuantity() <= reorderPoint : null
                ))
                .collect(Collectors.toList());

        return new ReorderPointResponseDto(
                product.getId(), product.getSku(), product.getName(),
                ss.getStatus(), ss.getNote(), ss.getLeadTimeDays(), ss.getMeanDailyDemand(),
                ss.getSafetyStock(), reorderPoint != null ? round2(reorderPoint) : null,
                warehouseStocks
        );
    }

    private static Double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    //dead stock logic
    private static final int DEFAULT_DEAD_STOCK_THRESHOLD_DAYS = 90;

    public List<DeadStockResponseDto> detectDeadStock(int thresholdDays) {
        List<Inventory> allInventory = inventoryRepository.findAll();
        Map<Long, List<Inventory>> inventoryByProduct = allInventory.stream()
                .collect(Collectors.groupingBy(inv -> inv.getProduct().getId()));

        Map<Long, LocalDate> lastSaleDateByProduct = computeLastSaleDates();
        LocalDate today = LocalDate.now();

        List<DeadStockResponseDto> results = new ArrayList<>();

        for (Map.Entry<Long, List<Inventory>> entry : inventoryByProduct.entrySet()) {
            List<Inventory> invList = entry.getValue();
            int totalQty = invList.stream().mapToInt(Inventory::getQuantity).sum();

            if (totalQty <= 0) continue; // no stock on hand -> not "dead stock", just out of stock

            Product product = invList.get(0).getProduct();
            LocalDate lastSaleDate = lastSaleDateByProduct.get(entry.getKey());

            Integer daysSinceLastSale = lastSaleDate != null
                    ? (int) ChronoUnit.DAYS.between(lastSaleDate, today)
                    : null;

            boolean isDead = lastSaleDate == null || daysSinceLastSale >= thresholdDays;

            String note = lastSaleDate == null
                    ? "No shipped sales found for this product — never sold."
                    : null;

            List<WarehouseQuantityDto> warehouseStocks = invList.stream()
                    .map(inv -> new WarehouseQuantityDto(
                            inv.getWarehouse().getId(),
                            inv.getWarehouse().getName(),
                            inv.getQuantity()
                    ))
                    .collect(Collectors.toList());

            results.add(new DeadStockResponseDto(
                    product.getId(), product.getSku(), product.getName(),
                    totalQty, lastSaleDate, daysSinceLastSale, thresholdDays, isDead, note,
                    warehouseStocks
            ));
        }

        return results;
    }

    private Map<Long, LocalDate> computeLastSaleDates() {
        List<SalesOrder> shippedOrders = getShippedSalesOrders();
        Map<Long, LocalDate> lastSale = new HashMap<>();

        for (SalesOrder so : shippedOrders) {
            LocalDate date = so.getOrderDate();
            for (SalesOrderItem item : so.getItems()) {
                Long productId = item.getProduct().getId();
                LocalDate current = lastSale.get(productId);
                if (current == null || date.isAfter(current)) {
                    lastSale.put(productId, date);
                }
            }
        }
        return lastSale;
    }

    //supplier order logic
    public List<SupplierAnalyticsResponseDto> calculateSupplierAnalytics() {
        List<Supplier> suppliers = supplierRepository.findAll();
        List<PurchaseOrder> allPOs = purchaseOrderRepository.findAll();

        Map<Long, List<PurchaseOrder>> posBySupplier = allPOs.stream()
                .collect(Collectors.groupingBy(po -> po.getSupplier().getId()));

        return suppliers.stream()
                .map(s -> buildSupplierAnalytics(s, posBySupplier.getOrDefault(s.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private SupplierAnalyticsResponseDto buildSupplierAnalytics(Supplier supplier, List<PurchaseOrder> pos) {
        int total = pos.size();

        List<PurchaseOrder> received = pos.stream()
                .filter(po -> "RECEIVED".equals(po.getStatus()) && po.getReceivedDate() != null)
                .collect(Collectors.toList());
        int receivedCount = received.size();

        int pendingCount = (int) pos.stream()
                .filter(po -> "PENDING".equals(po.getStatus()))
                .count();

        int receivedWithoutDateCount = (int) pos.stream()
                .filter(po -> "RECEIVED".equals(po.getStatus()) && po.getReceivedDate() == null)
                .count();

        if (receivedCount == 0) {
            return SupplierAnalyticsResponseDto.noReceivedOrders(supplier, total, pendingCount, receivedWithoutDateCount);
        }

        double avgActualLeadTime = received.stream()
                .mapToLong(po -> ChronoUnit.DAYS.between(po.getOrderDate(), po.getReceivedDate()))
                .average()
                .orElse(0.0);

        Integer statedLeadTime = supplier.getLeadTimeDays();

        if (statedLeadTime == null) {
            return SupplierAnalyticsResponseDto.missingLeadTime(supplier, total, receivedCount, pendingCount,
                    receivedWithoutDateCount, avgActualLeadTime);
        }

        long onTimeCount = received.stream()
                .filter(po -> ChronoUnit.DAYS.between(po.getOrderDate(), po.getReceivedDate()) <= statedLeadTime)
                .count();

        double onTimeRate = (onTimeCount * 100.0) / receivedCount;

        return SupplierAnalyticsResponseDto.ok(supplier, total, receivedCount, pendingCount,
                receivedWithoutDateCount, avgActualLeadTime, onTimeRate);
    }

}