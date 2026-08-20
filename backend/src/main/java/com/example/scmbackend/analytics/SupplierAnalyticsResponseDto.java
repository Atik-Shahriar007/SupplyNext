package com.example.scmbackend.analytics;

import com.example.scmbackend.supplier.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierAnalyticsResponseDto {
    private Long supplierId;
    private String supplierName;
    private String status; // "OK", "MISSING_LEAD_TIME", "NO_RECEIVED_ORDERS"
    private String note;
    private Integer statedLeadTimeDays;
    private Integer totalPurchaseOrders;
    private Integer receivedPurchaseOrders;
    private Integer pendingPurchaseOrders;
    private Double averageActualLeadTimeDays;
    private Double onTimeDeliveryRate;
    private Double performanceScore;

    public static SupplierAnalyticsResponseDto ok(Supplier s, int total, int received, int pending,
                                                  double avgActualLeadTime, double onTimeRate) {
        return new SupplierAnalyticsResponseDto(
                s.getId(), s.getName(), "OK", null,
                s.getLeadTimeDays(), total, received, pending,
                round2(avgActualLeadTime), round2(onTimeRate), round2(onTimeRate)
        );
    }

    public static SupplierAnalyticsResponseDto missingLeadTime(Supplier s, int total, int received, int pending,
                                                               double avgActualLeadTime) {
        return new SupplierAnalyticsResponseDto(
                s.getId(), s.getName(), "MISSING_LEAD_TIME",
                "Set leadTimeDays on this supplier to calculate on-time delivery rate and performance score.",
                null, total, received, pending, round2(avgActualLeadTime), null, null
        );
    }

    public static SupplierAnalyticsResponseDto noReceivedOrders(Supplier s, int total, int pending) {
        return new SupplierAnalyticsResponseDto(
                s.getId(), s.getName(), "NO_RECEIVED_ORDERS",
                "No received purchase orders yet — performance can't be measured until at least one PO is received.",
                s.getLeadTimeDays(), total, 0, pending, null, null, null
        );
    }

    private static Double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}