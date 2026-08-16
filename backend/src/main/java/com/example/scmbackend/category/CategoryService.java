package com.example.scmbackend.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<CategoryResponseDto> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::toResponseDto);
    }

    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        Category saved = categoryRepository.save(category);
        return toResponseDto(saved);
    }

    private CategoryResponseDto toResponseDto(Category category) {
        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
