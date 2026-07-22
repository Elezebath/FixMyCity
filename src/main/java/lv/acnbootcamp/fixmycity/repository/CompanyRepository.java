package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    List<Company> findAllByActiveTrue();

    boolean existsByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyNameIgnoreCaseAndCompanyIdNot(
            String companyName,
            Long companyId);

    boolean existsByRegistrationNoAndCompanyIdNot(
            String registrationNo,
            Long companyId);
}
