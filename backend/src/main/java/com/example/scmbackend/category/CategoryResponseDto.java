package com.example.scmbackend.category;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class CategoryResponseDto {
    private Long id;
    private String name;
    private String description;
}