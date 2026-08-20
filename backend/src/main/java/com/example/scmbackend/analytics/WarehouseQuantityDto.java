package com.example.scmbackend.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WarehouseQuantityDto {
    private Long warehouseId;
    private String warehouseName;
    private Integer quantity;
}