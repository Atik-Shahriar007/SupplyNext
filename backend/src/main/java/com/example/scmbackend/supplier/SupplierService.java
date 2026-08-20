package com.example.scmbackend.supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    public Page<SupplierResponseDto> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(this::toResponseDto);
    }

    public SupplierResponseDto createSupplier(SupplierRequestDto dto) {
        Supplier supplier = new Supplier();
        applyDto(supplier, dto);

        Supplier saved = supplierRepository.save(supplier);
        return toResponseDto(saved);
    }

    public SupplierResponseDto updateSupplier(Long id, SupplierRequestDto dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));

        applyDto(supplier, dto);

        Supplier saved = supplierRepository.save(supplier);
        return toResponseDto(saved);
    }

    private void applyDto(Supplier supplier, SupplierRequestDto dto) {
        supplier.setName(dto.getName());
        supplier.setContactPerson(dto.getContactPerson());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());
        supplier.setLeadTimeDays(dto.getLeadTimeDays());
    }

    private SupplierResponseDto toResponseDto(Supplier supplier) {
        return new SupplierResponseDto(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactPerson(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddress(),
                supplier.getLeadTimeDays()
        );
    }
}
