package com.example.scmbackend.warehouse;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class WarehouseResponseDto {
    private Long id;
    private String name;
    private String location;
    private Integer capacity;
}
