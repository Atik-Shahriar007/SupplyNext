package com.example.scmbackend.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupplierRequestDto {
    @NotBlank(message = "Supplier name is required")
    private String name;

    private String contactPerson;
    private String phone;

    @Email(message = "Email must be valid")
    private String email;

    private String address;

    @NotNull(message = "Lead time (days) is required")
    @Positive(message = "Lead time must be greater than 0")
    private Integer leadTimeDays;
}
