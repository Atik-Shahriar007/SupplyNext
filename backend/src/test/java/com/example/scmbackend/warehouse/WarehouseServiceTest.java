package com.example.scmbackend.warehouse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void createWarehouse_shouldSaveAndReturnDto() {
        WarehouseRequestDto requestDto = new WarehouseRequestDto();
        requestDto.setName("Main Warehouse");
        requestDto.setLocation("Dhaka");
        requestDto.setCapacity(5000);

        Warehouse savedWarehouse = new Warehouse();
        savedWarehouse.setId(1L);
        savedWarehouse.setName("Main Warehouse");
        savedWarehouse.setLocation("Dhaka");
        savedWarehouse.setCapacity(5000);

        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(savedWarehouse);

        WarehouseResponseDto result = warehouseService.createWarehouse(requestDto);

        assertEquals(1L, result.getId());
        assertEquals("Main Warehouse", result.getName());
        assertEquals("Dhaka", result.getLocation());
        assertEquals(5000, result.getCapacity());
    }

    @Test
    void getAllWarehouses_shouldReturnPagedResults() {
        Warehouse warehouse1 = new Warehouse();
        warehouse1.setId(1L);
        warehouse1.setName("Main Warehouse");
        warehouse1.setLocation("Dhaka");
        warehouse1.setCapacity(5000);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Warehouse> page = new PageImpl<>(List.of(warehouse1), pageable, 1);

        when(warehouseRepository.findAll(pageable)).thenReturn(page);

        Page<WarehouseResponseDto> result = warehouseService.getAllWarehouses(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Main Warehouse", result.getContent().get(0).getName());
    }
}