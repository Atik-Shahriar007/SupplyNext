package com.example.scmbackend.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TransferRequestDto {
    @NotNull(message = "Source warehouse ID is required")
    private Long fromWarehouseId;

    @NotNull(message = "Destination warehouse ID is required")
    private Long toWarehouseId;

    @NotNull(message = "Transfer date is required")
    private LocalDate transferDate;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<TransferItemDto> items;

    @AssertTrue(message = "Source and destination warehouses must be different")
    private boolean isWarehousesDifferent() {
        return fromWarehouseId == null || toWarehouseId == null || !fromWarehouseId.equals(toWarehouseId);
    }
}
