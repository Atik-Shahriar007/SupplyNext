package com.example.scmbackend.supplier;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public List<SupplierResponseDto> getAllSuppliers() {
        return supplierService.getAllSuppliers();
    }

    @PostMapping
    public SupplierResponseDto createSupplier(@Valid @RequestBody SupplierRequestDto dto) {
        return supplierService.createSupplier(dto);
    }
}