package com.example.scmbackend.category;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<CategoryResponseDto> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    public CategoryResponseDto createCategory(@Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.createCategory(dto);
    }
}