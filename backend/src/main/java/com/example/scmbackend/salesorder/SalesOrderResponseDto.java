package com.example.scmbackend.salesorder;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SalesOrderResponseDto {
    private Long id;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private LocalDate orderDate;
    private String status;
    private List<SalesOrderItemDto> items;
}