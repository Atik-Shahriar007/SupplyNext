package com.example.scmbackend.salesorder;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
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
public class SalesOrderService {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    public Page<SalesOrderResponseDto> getAllSalesOrders(Pageable pageable) {
        return salesOrderRepository.findAll(pageable).map(this::toResponseDto);
    }

    public SalesOrderResponseDto createSalesOrder(SalesOrderRequestDto dto) {
        Warehouse warehouse = warehouseRepository.findById(dto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        SalesOrder so = new SalesOrder();
        so.setCustomerName(dto.getCustomerName());
        so.setWarehouse(warehouse);
        so.setOrderDate(dto.getOrderDate());
        so.setStatus("PENDING");

        List<SalesOrderItem> items = dto.getItems().stream().map(itemDto -> {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            SalesOrderItem item = new SalesOrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setSalesOrder(so);
            return item;
        }).collect(Collectors.toList());

        so.setItems(items);

        SalesOrder saved = salesOrderRepository.save(so);
        return toResponseDto(saved);
    }

    public SalesOrderResponseDto shipSalesOrder(Long id) {
        SalesOrder so = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        if ("SHIPPED".equals(so.getStatus())) {
            throw new RuntimeException("This sales order was already shipped");
        }

        for (SalesOrderItem item : so.getItems()) {
            Optional<Inventory> inventoryOpt = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), so.getWarehouse().getId());

            if (inventoryOpt.isEmpty() || inventoryOpt.get().getQuantity() < item.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock for product ID " + item.getProduct().getId()
                );
            }
        }

        for (SalesOrderItem item : so.getItems()) {
            Inventory inventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), so.getWarehouse().getId())
                    .orElseThrow();

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
        }

        so.setStatus("SHIPPED");
        SalesOrder saved = salesOrderRepository.save(so);
        return toResponseDto(saved);
    }

    private SalesOrderResponseDto toResponseDto(SalesOrder so) {
        List<SalesOrderItemDto> itemDtos = so.getItems().stream()
                .map(item -> new SalesOrderItemDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        return new SalesOrderResponseDto(
                so.getId(),
                so.getCustomerName(),
                so.getWarehouse().getId(),
                so.getWarehouse().getName(),
                so.getOrderDate(),
                so.getStatus(),
                itemDtos
        );
    }
}