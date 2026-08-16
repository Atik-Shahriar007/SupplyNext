package com.example.scmbackend.category;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Page<CategoryResponseDto> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return categoryService.getAllCategories(PageRequest.of(page, size));
    }

    @PostMapping
    public CategoryResponseDto createCategory(@Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.createCategory(dto);
    }
}