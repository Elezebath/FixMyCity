package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.category.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse createCategory(String name, String description);
    CategoryResponse updateCategory(Long id, String name, String description);
    void deleteCategory(Long id);
}