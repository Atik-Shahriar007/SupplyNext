package com.example.scmbackend.transfer;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import com.example.scmbackend.product.Product;
import com.example.scmbackend.product.ProductRepository;
import com.example.scmbackend.warehouse.Warehouse;
import com.example.scmbackend.warehouse.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<TransferResponseDto> getAllTransfers() {
        return transferRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public TransferResponseDto createTransfer(TransferRequestDto dto) {
        Warehouse fromWarehouse = warehouseRepository.findById(dto.getFromWarehouseId())
                .orElseThrow(() -> new RuntimeException("Source warehouse not found"));
        Warehouse toWarehouse = warehouseRepository.findById(dto.getToWarehouseId())
                .orElseThrow(() -> new RuntimeException("Destination warehouse not found"));

        Transfer transfer = new Transfer();
        transfer.setFromWarehouse(fromWarehouse);
        transfer.setToWarehouse(toWarehouse);
        transfer.setTransferDate(dto.getTransferDate());
        transfer.setStatus("PENDING");

        List<TransferItem> items = dto.getItems().stream().map(itemDto -> {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

            TransferItem item = new TransferItem();
            item.setProduct(product);
            item.setQuantity(itemDto.getQuantity());
            item.setTransfer(transfer);
            return item;
        }).collect(Collectors.toList());

        transfer.setItems(items);

        Transfer saved = transferRepository.save(transfer);
        return toResponseDto(saved);
    }

    public TransferResponseDto completeTransfer(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if ("COMPLETED".equals(transfer.getStatus())) {
            throw new RuntimeException("This transfer was already completed");
        }

        for (TransferItem item : transfer.getItems()) {
            Optional<Inventory> sourceInventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), transfer.getFromWarehouse().getId());

            if (sourceInventory.isEmpty() || sourceInventory.get().getQuantity() < item.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock in source warehouse for product ID " + item.getProduct().getId()
                );
            }
        }

        for (TransferItem item : transfer.getItems()) {
            Inventory sourceInventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), transfer.getFromWarehouse().getId())
                    .orElseThrow();
            sourceInventory.setQuantity(sourceInventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(sourceInventory);

            Optional<Inventory> destInventoryOpt = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), transfer.getToWarehouse().getId());

            if (destInventoryOpt.isPresent()) {
                Inventory destInventory = destInventoryOpt.get();
                destInventory.setQuantity(destInventory.getQuantity() + item.getQuantity());
                inventoryRepository.save(destInventory);
            } else {
                Inventory newInventory = new Inventory();
                newInventory.setProduct(item.getProduct());
                newInventory.setWarehouse(transfer.getToWarehouse());
                newInventory.setQuantity(item.getQuantity());
                inventoryRepository.save(newInventory);
            }
        }

        transfer.setStatus("COMPLETED");
        Transfer saved = transferRepository.save(transfer);
        return toResponseDto(saved);
    }

    private TransferResponseDto toResponseDto(Transfer transfer) {
        List<TransferItemDto> itemDtos = transfer.getItems().stream()
                .map(item -> new TransferItemDto(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        return new TransferResponseDto(
                transfer.getId(),
                transfer.getFromWarehouse().getId(),
                transfer.getFromWarehouse().getName(),
                transfer.getToWarehouse().getId(),
                transfer.getToWarehouse().getName(),
                transfer.getTransferDate(),
                transfer.getStatus(),
                itemDtos
        );
    }
}
