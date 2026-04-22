package com.example.homestaymanager.service;

import com.example.homestaymanager.model.Category;

public interface CategoryService {

    Integer createCategory(Category category);

    Category getCategoryByID(int id);

    void deleteCategoryById(int id);
}