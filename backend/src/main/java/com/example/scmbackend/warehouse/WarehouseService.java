package com.example.scmbackend.warehouse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    public Page<WarehouseResponseDto> getAllWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(this::toResponseDto);
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