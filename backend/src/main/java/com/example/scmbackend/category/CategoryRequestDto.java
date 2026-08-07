package com.example.scmbackend.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequestDto {
    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}
