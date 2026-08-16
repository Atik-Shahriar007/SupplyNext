package com.example.scmbackend.transfer;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping
    public Page<TransferResponseDto> getAllTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return transferService.getAllTransfers(PageRequest.of(page, size));
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
