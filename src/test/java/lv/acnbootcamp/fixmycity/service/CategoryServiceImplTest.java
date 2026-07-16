package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.category.CategoryResponse;
import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.exception.category.CategoryAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryInUseException;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.IncidentRepository;
import lv.acnbootcamp.fixmycity.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Category buildCategory(Long id, String name) {
        return Category.builder()
                .categoryId(id)
                .name(name)
                .description("Some description")
                .isDeleted(false)
                .build();
    }

    @Nested
    class GetAllCategories {

        @Test
        void returnsAllCategoriesMappedToResponses() {
            when(categoryRepository.findAllByIsDeletedFalse())
                    .thenReturn(List.of(buildCategory(1L, "Roads"), buildCategory(2L, "Lighting")));

            List<CategoryResponse> result = categoryService.getAllCategories();

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().getName()).isEqualTo("Roads");
        }
    }

    @Nested
    class GetCategoryById {

        @Test
        void returnsCategory_whenFound() {
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(1L))
                    .thenReturn(Optional.of(buildCategory(1L, "Roads")));

            CategoryResponse result = categoryService.getCategoryById(1L);

            assertThat(result.getCategoryId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Roads");
        }

        @Test
        void throwsNotFound_whenMissing() {
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                    .isInstanceOf(CategoryNotFoundException.class);
        }
    }

    @Nested
    class CreateCategory {

        @Test
        void createsCategory_whenNameIsUnique() {
            when(categoryRepository.existsByNameIgnoreCase("Roads")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> {
                Category c = inv.getArgument(0);
                c.setCategoryId(1L);
                return c;
            });

            CategoryResponse result = categoryService.createCategory("Roads", "Road issues");

            assertThat(result.getCategoryId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Roads");
            verify(categoryRepository).save(any(Category.class));
        }

        @Test
        void throwsAlreadyExists_whenNameTaken() {
            when(categoryRepository.existsByNameIgnoreCase("Roads")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory("Roads", "Road issues"))
                    .isInstanceOf(CategoryAlreadyExistsException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    class UpdateCategory {

        @Test
        void updatesNameAndDescription_whenNoConflict() {
            Category existing = buildCategory(1L, "Roads");
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));
            when(categoryRepository.existsByNameIgnoreCase("Roads & Potholes")).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

            CategoryResponse result = categoryService.updateCategory(1L, "Roads & Potholes", "Updated desc");

            assertThat(result.getName()).isEqualTo("Roads & Potholes");
            assertThat(result.getDescription()).isEqualTo("Updated desc");
        }

        @Test
        void throwsNotFound_whenCategoryMissing() {
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.updateCategory(99L, "Roads", "desc"))
                    .isInstanceOf(CategoryNotFoundException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        void throwsAlreadyExists_whenRenamingToAnotherExistingName() {
            Category existing = buildCategory(1L, "Roads");
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));
            when(categoryRepository.existsByNameIgnoreCase("Lighting")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.updateCategory(1L, "Lighting", "desc"))
                    .isInstanceOf(CategoryAlreadyExistsException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        void allowsUpdate_whenNameUnchanged() {
            Category existing = buildCategory(1L, "Roads");
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

            categoryService.updateCategory(1L, "Roads", "New description");

            verify(categoryRepository, never()).existsByNameIgnoreCase(anyString());
            verify(categoryRepository).save(any(Category.class));
        }
    }

    @Nested
    class DeleteCategory {

        @Test
        void softDeletesCategory_whenNotReferenced() {
            Category existing = buildCategory(1L, "Roads");
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));
            when(incidentRepository.existsBySoftDeletedFalseAndCategory_CategoryId(1L)).thenReturn(false);
            when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

            categoryService.deleteCategory(1L);

            ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
            verify(categoryRepository).save(captor.capture());
            assertThat(captor.getValue().getIsDeleted()).isTrue();

            verify(categoryRepository, never()).delete(any(Category.class));
        }

        @Test
        void throwsNotFound_whenCategoryMissing() {
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                    .isInstanceOf(CategoryNotFoundException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        void throwsInUse_whenReferencedByIncidents() {
            Category existing = buildCategory(1L, "Roads");
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(existing));
            when(incidentRepository.existsBySoftDeletedFalseAndCategory_CategoryId(1L)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                    .isInstanceOf(CategoryInUseException.class);

            verify(categoryRepository, never()).save(any(Category.class));
        }
    }
}