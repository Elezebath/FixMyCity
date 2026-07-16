package lv.acnbootcamp.fixmycity.util;

import lv.acnbootcamp.fixmycity.entity.Category;

/**
 * Fluent builder for constructing Category test fixtures with sensible defaults.
 */
public class CategoryTestDataBuilder {

    private Long categoryId = 1L;
    private String name = "Infrastructure";
    private String description = "Infrastructure related issues";

    private CategoryTestDataBuilder() {}

    public static CategoryTestDataBuilder aCategory() {
        return new CategoryTestDataBuilder();
    }

    public CategoryTestDataBuilder withId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public CategoryTestDataBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CategoryTestDataBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public Category build() {
        return Category.builder()
                .categoryId(categoryId)
                .name(name)
                .description(description)
                .build();
    }
}