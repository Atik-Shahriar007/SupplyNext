package com.example.scmbackend.purchaseorder;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.supplier.Supplier;
import com.example.scmbackend.supplier.SupplierRepository;
import com.example.scmbackend.warehouse.Warehouse;
import com.example.scmbackend.warehouse.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    public Page<PurchaseOrderResponseDto> getAllPurchaseOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable).map(this::toResponseDto);
    }

    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto dto) {
        Supplier supplier = supplierRepository.findById(dto.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        PurchaseOrder po = new PurchaseOrder();
        po.setSupplier(supplier);
        po.setWarehouse(warehouse);
        po.setOrderDate(dto.getOrderDate());
        po.setStatus("PENDING");

        List<PurchaseOrderItem> items = dto.getItems().stream().map(itemDto -> {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setPurchaseOrder(po);
            return item;
        }).collect(Collectors.toList());

        po.setItems(items);

        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return toResponseDto(saved);
    }

    public PurchaseOrderResponseDto receivePurchaseOrder(Long id) {
        PurchaseOrder po = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if ("RECEIVED".equals(po.getStatus())) {
            throw new RuntimeException("This purchase order was already received");
        }

        for (PurchaseOrderItem item : po.getItems()) {
            Optional<Inventory> existingInventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), po.getWarehouse().getId());

            if (existingInventory.isPresent()) {
                Inventory inventory = existingInventory.get();
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                inventoryRepository.save(inventory);
            } else {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(item.getProduct());
                newInventory.setWarehouse(po.getWarehouse());
                newInventory.setQuantity(item.getQuantity());
                inventoryRepository.save(newInventory);
            }
        }
        po.setReceivedDate(java.time.LocalDate.now());
        po.setStatus("RECEIVED");
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return toResponseDto(saved);
    }

    private PurchaseOrderResponseDto toResponseDto(PurchaseOrder po) {
        List<PurchaseOrderItemDto> itemDtos = po.getItems().stream()
                .map(item -> new PurchaseOrderItemDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        return new PurchaseOrderResponseDto(
                po.getId(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getWarehouse().getId(),
                po.getWarehouse().getName(),
                po.getOrderDate(),
                po.getReceivedDate(),
                po.getStatus(),
                itemDtos
        );
    }
}