package com.example.scmbackend.purchaseorder;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
}
