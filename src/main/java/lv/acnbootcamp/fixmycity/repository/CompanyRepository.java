package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
