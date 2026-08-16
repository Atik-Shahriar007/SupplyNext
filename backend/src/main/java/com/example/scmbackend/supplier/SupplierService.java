package com.example.scmbackend.supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public Page<SupplierResponseDto> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(this::toResponseDto);
    }

    public SupplierResponseDto createSupplier(SupplierRequestDto dto) {
        Supplier supplier = new Supplier();
        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());

        Supplier saved = supplierRepository.save(supplier);
        return toResponseDto(saved);
    }

    private SupplierResponseDto toResponseDto(Supplier supplier) {
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress()
        );
    }
}
