package com.example.scmbackend.analytics;

import com.example.scmbackend.supplier.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupplierAnalyticsResponseDto {
    private Long supplierId;
    private String supplierName;
    private String status;
    private String note;
    private Integer statedLeadTimeDays;
    private Integer totalPurchaseOrders;
    private Integer receivedPurchaseOrders;
    private Integer pendingPurchaseOrders;
    private Integer receivedWithoutDateCount; // legacy RECEIVED POs from before receivedDate existed
    private Double averageActualLeadTimeDays;
    private Double onTimeDeliveryRate;
    private Double performanceScore;

    public static SupplierAnalyticsResponseDto ok(Supplier s, int total, int received, int pending,
                                                  int receivedWithoutDate, double avgActualLeadTime, double onTimeRate) {
        return new SupplierAnalyticsResponseDto(
                s.getId(), s.getName(), "OK", null,
                s.getLeadTimeDays(), total, received, pending, receivedWithoutDate,
                round2(avgActualLeadTime), round2(onTimeRate), round2(onTimeRate)
        );
    }

    public static SupplierAnalyticsResponseDto missingLeadTime(Supplier s, int total, int received, int pending,
                                                               int receivedWithoutDate, double avgActualLeadTime) {
        return new SupplierAnalyticsResponseDto(
                s.getId(), s.getName(), "MISSING_LEAD_TIME",
                "Set leadTimeDays on this supplier to calculate on-time delivery rate and performance score.",
                null, total, received, pending, receivedWithoutDate, round2(avgActualLeadTime), null, null
        );
    }

    public static SupplierAnalyticsResponseDto noReceivedOrders(Supplier s, int total, int pending, int receivedWithoutDate) {
        String note = receivedWithoutDate > 0
                ? receivedWithoutDate + " order(s) were received before lead-time tracking was added and can't be used for performance measurement."
                : "No received purchase orders yet — performance can't be measured until at least one PO is received.";
        return new SupplierAnalyticsResponseDto(
                s.getId(), s.getName(), "NO_RECEIVED_ORDERS", note,
                s.getLeadTimeDays(), total, 0, pending, receivedWithoutDate, null, null, null
        );
    }

    private static Double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}