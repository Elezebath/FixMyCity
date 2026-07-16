package lv.acnbootcamp.fixmycity.service.impl;

import lombok.extern.slf4j.Slf4j;
import lv.acnbootcamp.fixmycity.dto.category.CategoryResponse;
import lv.acnbootcamp.fixmycity.entity.AuditAction;
import lv.acnbootcamp.fixmycity.entity.AuditEntityType;
import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.exception.category.CategoryAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryInUseException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.IncidentRepository;
import lv.acnbootcamp.fixmycity.service.AuditLogService;
import lv.acnbootcamp.fixmycity.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final IncidentRepository incidentRepository;
    private final AuditLogService auditLogService;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                               IncidentRepository incidentRepository,
                               AuditLogService auditLogService) {
        this.categoryRepository = categoryRepository;
        this.incidentRepository = incidentRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllByIsDeletedFalse().stream()
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
        auditLogService.log(AuditEntityType.CATEGORY, saved.getCategoryId(), AuditAction.CREATE,
                "Created category '" + saved.getName() + "'");
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

        String oldName = category.getName();
        category.setName(name);
        category.setDescription(description);

        Category saved = categoryRepository.save(category);
        auditLogService.log(AuditEntityType.CATEGORY, saved.getCategoryId(), AuditAction.UPDATE,
                "Renamed '" + oldName + "' -> '" + saved.getName() + "'");
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

        category.setIsDeleted(true);
        categoryRepository.save(category);
        auditLogService.log(AuditEntityType.CATEGORY, id, AuditAction.DELETE,
                "Soft-deleted category '" + category.getName() + "'");
    }

    private Category findOrThrow(Long id) {
        return categoryRepository.findByCategoryIdAndIsDeletedFalse(id)
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
