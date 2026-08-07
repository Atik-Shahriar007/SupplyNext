package com.example.scmbackend.warehouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    public List<WarehouseResponseDto> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public WarehouseResponseDto createWarehouse(WarehouseRequestDto dto) {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(dto.getName());
        warehouse.setLocation(dto.getLocation());
        warehouse.setCapacity(dto.getCapacity());

        Warehouse saved = warehouseRepository.save(warehouse);
        return toResponseDto(saved);
    }

    private WarehouseResponseDto toResponseDto(Warehouse warehouse) {
        return new WarehouseResponseDto(
                warehouse.getId(),
                warehouse.getName(),
                warehouse.getLocation(),
                warehouse.getCapacity()
        );
    }
}