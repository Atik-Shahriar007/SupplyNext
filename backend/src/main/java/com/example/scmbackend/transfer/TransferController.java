package com.example.scmbackend.transfer;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping
    public List<Transfer> getAllTransfers() {
        return transferService.getAllTransfers();
    }

    @PostMapping
    public Transfer createTransfer(@Valid @RequestBody Transfer transfer) {
        return transferService.createTransfer(transfer);
    }

    @PatchMapping("/{id}/complete")
    public Transfer completeTransfer(@PathVariable Long id) {
        return transferService.completeTransfer(id);
    }
}
