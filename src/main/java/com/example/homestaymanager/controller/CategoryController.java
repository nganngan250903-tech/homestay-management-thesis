package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateCategoryRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.model.Category;
import com.example.homestaymanager.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getListCategory() {
        List<Category> categories = categoryService.getListCategory();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách danh mục thành công", categories);
    }

    @PostMapping("/categories")
    public ApiResponse<Integer> createCategory(@RequestBody Category category) {
        int i = categoryService.createCategory(category);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, category.getId());
    }

    @GetMapping("/categories/{categoryId}")
    public ApiResponse<Category> getCategoryById(@PathVariable int categoryId) {
        Category category = categoryService.getCategoryByID(categoryId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, category);
    }

    @PatchMapping("/categories/{categoryId}")
    public ApiResponse<Category> updateCategoryById(@PathVariable int categoryId, @RequestBody UpdateCategoryRequest request) {
        Category category = categoryService.updateCategoryById(categoryId, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, category);
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<?> deleteCategoryById(@PathVariable int categoryId) {
        categoryService.deleteCategoryById(categoryId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}