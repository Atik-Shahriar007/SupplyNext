package com.example.scmbackend.supplier;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public Page<SupplierResponseDto> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return supplierService.getAllSuppliers(PageRequest.of(page, size));
    }

    @PostMapping
    public SupplierResponseDto createSupplier(@Valid @RequestBody SupplierRequestDto dto) {
        return supplierService.createSupplier(dto);
    }
    @PatchMapping("/{id}")
    public SupplierResponseDto updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierRequestDto dto) {
        return supplierService.updateSupplier(id, dto);
    }
}