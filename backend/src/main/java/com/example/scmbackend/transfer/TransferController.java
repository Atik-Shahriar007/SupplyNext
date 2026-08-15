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
    public List<TransferResponseDto> getAllTransfers() {
        return transferService.getAllTransfers();
    }

    @PostMapping
    public TransferResponseDto createTransfer(@Valid @RequestBody TransferRequestDto dto) {
        return transferService.createTransfer(dto);
    }

    @PatchMapping("/{id}/complete")
    public TransferResponseDto completeTransfer(@PathVariable Long id) {
        return transferService.completeTransfer(id);
    }
}
