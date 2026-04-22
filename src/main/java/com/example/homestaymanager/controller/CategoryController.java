package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.Category;
import com.example.homestaymanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/categories")
    public Integer createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    @GetMapping("/categories/{categoryId}")
    public Category getCategoryById(@PathVariable int categoryId) {
        return categoryService.getCategoryByID(categoryId);
    }

    @DeleteMapping("/categories/{categoryId}")
    public void deleteCategoryById(@PathVariable int categoryId) {
        categoryService.deleteCategoryById(categoryId);
    }
}