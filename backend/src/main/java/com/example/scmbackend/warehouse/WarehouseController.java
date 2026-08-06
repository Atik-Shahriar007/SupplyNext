package com.example.scmbackend.warehouse;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseRepository warehouseRepository;

    // GET all warehouses
    @GetMapping
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    // POST a new warehouse
    @PostMapping
    public Warehouse createWarehouse(@Valid @RequestBody Warehouse warehouse) {
        return warehouseRepository.save(warehouse);
    }
}