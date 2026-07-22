package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryRepositoryTest(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private Category activeCategory;
    private Category deletedCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        activeCategory = buildCategory("Roads", "Road related issues", false);
        deletedCategory = buildCategory("Parks", "Park related issues", true);

        activeCategory = categoryRepository.save(activeCategory);
        deletedCategory = categoryRepository.save(deletedCategory);
    }

    private Category buildCategory(String name, String description, boolean isDeleted) {
        return Category.builder()
                .name(name)
                .description(description)
                .isDeleted(isDeleted)
                .build();
    }

    @Test
    void existsByNameIgnoreCase_shouldReturnTrueWhenNameMatchesExactly() {
        boolean result = categoryRepository.existsByNameIgnoreCase("Roads");

        assertThat(result).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_shouldReturnTrueWhenNameMatchesIgnoringCase() {
        boolean result = categoryRepository.existsByNameIgnoreCase("rOaDs");

        assertThat(result).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_shouldReturnFalseWhenNameDoesNotExist() {
        boolean result = categoryRepository.existsByNameIgnoreCase("Lighting");

        assertThat(result).isFalse();
    }

    @Test
    void findAllByIsDeletedFalse_shouldReturnOnlyNonDeletedCategories() {
        List<Category> result = categoryRepository.findAllByIsDeletedFalse();

        assertThat(result)
                .hasSize(1)
                .containsExactly(activeCategory);
    }

    @Test
    void findAllByIsDeletedFalse_shouldReturnEmptyListWhenAllDeleted() {
        categoryRepository.deleteAll();
        categoryRepository.save(buildCategory("Waste", "Waste related issues", true));

        List<Category> result = categoryRepository.findAllByIsDeletedFalse();

        assertThat(result).isEmpty();
    }

    @Test
    void findByCategoryIdAndIsDeletedFalse_shouldReturnCategoryWhenNotDeleted() {
        Optional<Category> result = categoryRepository
                .findByCategoryIdAndIsDeletedFalse(activeCategory.getCategoryId());

        assertThat(result)
                .isPresent()
                .contains(activeCategory);
    }

    @Test
    void findByCategoryIdAndIsDeletedFalse_shouldReturnEmptyWhenDeleted() {
        Optional<Category> result = categoryRepository
                .findByCategoryIdAndIsDeletedFalse(deletedCategory.getCategoryId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByCategoryIdAndIsDeletedFalse_shouldReturnEmptyWhenIdDoesNotExist() {
        Optional<Category> result = categoryRepository.findByCategoryIdAndIsDeletedFalse(999L);

        assertThat(result).isEmpty();
    }
}