package com.example.scmbackend.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WarehouseRequestDto {
    @NotBlank(message = "Warehouse name is required")
    private String name;

    @NotBlank(message = "Location is required")
    private String location;

    @Positive(message = "Capacity must be greater than 0")
    private Integer capacity;
}