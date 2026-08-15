package com.example.scmbackend.transfer;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TransferResponseDto {
    private Long id;
    private Long fromWarehouseId;
    private String fromWarehouseName;
    private Long toWarehouseId;
    private String toWarehouseName;
    private LocalDate transferDate;
    private String status;
    private List<TransferItemDto> items;
}
