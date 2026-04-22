package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.model.Category;
import com.example.homestaymanager.repository.CategoryRepository;
import com.example.homestaymanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Integer createCategory(Category category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new RuntimeException("Category name không được để trống");
        }

        categoryRepository.save(category);
        return category.getId();
    }

    @Override
    public Category getCategoryByID(int id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Override
    public void deleteCategoryById(int id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryRepository.delete(category);
    }
}