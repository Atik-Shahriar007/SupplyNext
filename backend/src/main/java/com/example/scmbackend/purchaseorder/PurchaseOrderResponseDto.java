package com.example.scmbackend.purchaseorder;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PurchaseOrderResponseDto {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private Long warehouseId;
    private String warehouseName;
    private LocalDate orderDate;
    private LocalDate receivedDate;
    private String status;
    private List<PurchaseOrderItemDto> items;
}
