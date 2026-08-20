package com.example.scmbackend.analytics;

import com.example.scmbackend.product.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EOQResponseDto {
    private Long productId;
    private String sku;
    private String productName;
    private String status;   // "OK", "MISSING_COST_DATA", "INSUFFICIENT_DATA"
    private String note;
    private Double annualDemand;
    private Double unitCost;
    private Double holdingCostRate;
    private Double orderingCost;
    private Double holdingCostPerUnit;
    private Double eoq;
    private Integer recommendedOrdersPerYear;

    public static EOQResponseDto ok(Product p, double annualDemand, double holdingCostPerUnit, double eoq, String note) {
        int ordersPerYear = eoq > 0 ? (int) Math.round(annualDemand / eoq) : 0;
        return new EOQResponseDto(
                p.getId(), p.getSku(), p.getName(), "OK", note,
                round2(annualDemand), p.getUnitCost(), p.getHoldingCostRate(), p.getOrderingCost(),
                round2(holdingCostPerUnit), round2(eoq), ordersPerYear
        );
    }

    public static EOQResponseDto missingCostData(Product p) {
        return new EOQResponseDto(
                p.getId(), p.getSku(), p.getName(), "MISSING_COST_DATA",
                "Set unit cost, holding cost rate, and ordering cost on this product to calculate EOQ.",
                null, p.getUnitCost(), p.getHoldingCostRate(), p.getOrderingCost(),
                null, null, null
        );
    }

    public static EOQResponseDto insufficientData(Product p) {
        return new EOQResponseDto(
                p.getId(), p.getSku(), p.getName(), "INSUFFICIENT_DATA",
                "No shipped sales history for this product yet — EOQ needs at least one shipped sales order.",
                null, p.getUnitCost(), p.getHoldingCostRate(), p.getOrderingCost(),
                null, null, null
        );
    }

    private static Double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
