package com.example.scmbackend.transfer;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping
    public List<Transfer> getAllTransfers() {
        return transferRepository.findAll();
    }

    @PostMapping
    public Transfer createTransfer(@Valid @RequestBody Transfer transfer) {
        if (transfer.getItems() != null) {
            transfer.getItems().forEach(item -> item.setTransfer(transfer));
        }
        transfer.setStatus("PENDING");
        return transferRepository.save(transfer);
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> completeTransfer(@PathVariable Long id) {
        Transfer transfer = transferRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if ("COMPLETED".equals(transfer.getStatus())) {
            return ResponseEntity.badRequest().body("This transfer was already completed");
        }

        // First pass: check source warehouse has enough stock for every item
        for (TransferItem item : transfer.getItems()) {
            Optional<Inventory> sourceInventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), transfer.getFromWarehouse().getId());

            if (sourceInventory.isEmpty() || sourceInventory.get().getQuantity() < item.getQuantity()) {
                return ResponseEntity.badRequest().body(
                        "Insufficient stock in source warehouse for product ID " + item.getProduct().getId()
                );
            }
        }

        // Second pass: decrease source, increase destination
        for (TransferItem item : transfer.getItems()) {
            // Decrease from source warehouse
            Inventory sourceInventory = inventoryRepository
                    .findByProductIdAndWarehouseId(item.getProduct().getId(), transfer.getFromWarehouse().getId())
                    .orElseThrow();
            sourceInventory.setQuantity(sourceInventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(sourceInventory);

            // Increase in destination warehouse (create record if it doesn't exist yet)
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
        transferRepository.save(transfer);

        return ResponseEntity.ok(transfer);
    }
}
