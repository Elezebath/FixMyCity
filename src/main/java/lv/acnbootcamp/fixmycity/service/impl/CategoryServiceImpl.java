package lv.acnbootcamp.fixmycity.service.impl;

import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.category.CategoryResponse;
import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.exception.category.CategoryAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryInUseException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.IncidentRepository;
import lv.acnbootcamp.fixmycity.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final IncidentRepository incidentRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository, IncidentRepository incidentRepository) {
        this.categoryRepository = categoryRepository;
        this.incidentRepository = incidentRepository;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(String name, String description) {
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new CategoryAlreadyExistsException("Category already exists with name: " + name);
        }

        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Created category id={} name={}", saved.getCategoryId(), saved.getName());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, String name, String description) {
        Category category = findOrThrow(id);

        if (!category.getName().equalsIgnoreCase(name)
                && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new CategoryAlreadyExistsException("Category already exists with name: " + name);
        }

        category.setName(name);
        category.setDescription(description);

        Category saved = categoryRepository.save(category);
        log.info("Updated category id={}", saved.getCategoryId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findOrThrow(id);

        if (incidentRepository.existsBySoftDeletedFalseAndCategory_CategoryId(id)) {
            throw new CategoryInUseException(
                    "Cannot delete category id " + id + ": it is still referenced by existing incidents");
        }

        categoryRepository.delete(category);
        log.info("Deleted category id={}", id);
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + id));
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .name(category.getName())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
