package com.example.scmbackend.transfer;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransferService {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    public Transfer createTransfer(Transfer transfer) {
        if (transfer.getItems() != null) {
            transfer.getItems().forEach(item -> item.setTransfer(transfer));
        }
        transfer.setStatus("PENDING");
        return transferRepository.save(transfer);
    }

    public Transfer completeTransfer(Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if ("COMPLETED".equals(transfer.getStatus())) {
            throw new RuntimeException("This transfer was already completed");
        }

        // First pass: check source warehouse has enough stock for every item
        for (TransferItem item : transfer.getItems()) {
            Optional<Inventory> sourceInventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), transfer.getFromWarehouse().getId());

            if (sourceInventory.isEmpty() || sourceInventory.get().getQuantity() < item.getQuantity()) {
                throw new RuntimeException(
                        "Insufficient stock in source warehouse for product ID " + item.getProduct().getId()
                );
            }
        }

        // Second pass: decrease source, increase destination
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
        return transferRepository.save(transfer);
    }
}
