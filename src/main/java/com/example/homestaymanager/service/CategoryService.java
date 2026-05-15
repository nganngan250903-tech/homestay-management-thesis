package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateCategoryRequest;
import com.example.homestaymanager.model.Category;
import java.util.List;

public interface CategoryService {

    Integer createCategory(Category category);

    Category getCategoryByID(int id);

    void deleteCategoryById(int id);

    List<Category> getListCategory();

    Category updateCategoryById(int id, UpdateCategoryRequest request);
}