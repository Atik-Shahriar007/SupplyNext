package com.example.scmbackend.purchaseorder;

import com.example.scmbackend.inventory.Inventory;
import com.example.scmbackend.inventory.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    public PurchaseOrder createPurchaseOrder(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getItems() != null) {
            purchaseOrder.getItems().forEach(item -> item.setPurchaseOrder(purchaseOrder));
        }
        purchaseOrder.setStatus("PENDING");
        return purchaseOrderRepository.save(purchaseOrder);
    }

    public PurchaseOrder receivePurchaseOrder(Long id) {
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

        po.setStatus("RECEIVED");
        return purchaseOrderRepository.save(po);
    }
}