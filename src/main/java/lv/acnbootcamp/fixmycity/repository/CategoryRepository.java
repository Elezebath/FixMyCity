package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<Category> findAllByIsDeletedFalse();
    Optional<Category> findByCategoryIdAndIsDeletedFalse(Long id);
}
