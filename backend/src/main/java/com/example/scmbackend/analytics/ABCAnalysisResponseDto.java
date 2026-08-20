package com.example.scmbackend.analytics;

import com.example.scmbackend.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ABCAnalysisResponseDto {
    private Long productId;
    private String sku;
    private String productName;
    private String status; // "OK" or "MISSING_COST_DATA"
    private Double annualDemand;
    private Double unitCost;
    private Double annualConsumptionValue;
    private Double percentOfTotalValue;
    private Double cumulativePercent;
    private String tier; // "A", "B", "C", or null

    public static ABCAnalysisResponseDto ok(Product p, double annualDemand, double value,
                                            double percentOfTotal, double cumulativePercent, String tier) {
        return new ABCAnalysisResponseDto(
                p.getId(), p.getSku(), p.getName(), "OK",
                round2(annualDemand), p.getUnitCost(), round2(value),
                round2(percentOfTotal), round2(cumulativePercent), tier
        );
    }

    public static ABCAnalysisResponseDto missingCostData(Product p) {
        return new ABCAnalysisResponseDto(
                p.getId(), p.getSku(), p.getName(), "MISSING_COST_DATA",
                null, p.getUnitCost(), null, null, null, null
        );
    }

    private static Double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
