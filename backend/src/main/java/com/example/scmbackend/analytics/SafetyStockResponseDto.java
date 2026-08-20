package com.example.scmbackend.analytics;

import com.example.scmbackend.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SafetyStockResponseDto {
    private Long productId;
    private String sku;
    private String productName;
    private String status; // "OK", "MISSING_LEAD_TIME", "INSUFFICIENT_DATA"
    private String note;
    private Long supplierId;
    private String supplierName;
    private Integer leadTimeDays;
    private Double serviceLevel;
    private Double zScore;
    private Double meanDailyDemand;
    private Double stdDevDailyDemand;
    private Double safetyStock;

    public static SafetyStockResponseDto ok(Product p, double serviceLevel, double z, int leadTimeDays,
                                            double meanDaily, double stdDevDaily, double safetyStock, String note) {
        return new SafetyStockResponseDto(
                p.getId(), p.getSku(), p.getName(), "OK", note,
                p.getSupplier().getId(), p.getSupplier().getName(), leadTimeDays,
                serviceLevel, z, round2(meanDaily), round2(stdDevDaily), round2(safetyStock)
        );
    }

    public static SafetyStockResponseDto missingLeadTime(Product p, double serviceLevel, double z) {
        return new SafetyStockResponseDto(
                p.getId(), p.getSku(), p.getName(), "MISSING_LEAD_TIME",
                "Set leadTimeDays on supplier '" + p.getSupplier().getName() + "' to calculate safety stock.",
                p.getSupplier().getId(), p.getSupplier().getName(),
                null, serviceLevel, z, null, null, null
        );
    }

    public static SafetyStockResponseDto insufficientData(Product p, double serviceLevel, double z, int leadTimeDays) {
        return new SafetyStockResponseDto(
                p.getId(), p.getSku(), p.getName(), "INSUFFICIENT_DATA",
                "Needs shipped sales history spanning at least 2 different days to estimate demand variability.",
                p.getSupplier().getId(), p.getSupplier().getName(), leadTimeDays,
                serviceLevel, z, null, null, null
        );
    }

    private static Double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}