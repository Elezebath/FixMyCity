package lv.acnbootcamp.fixmycity.repository;

import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.entity.Company;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CompanyRepositoryTest {

    private final CompanyRepository companyRepository;
    private final TestEntityManager entityManager;

    @Autowired
    public CompanyRepositoryTest(CompanyRepository companyRepository, TestEntityManager entityManager) {
        this.companyRepository = companyRepository;
        this.entityManager = entityManager;
    }

    private Category category;
    private Company activeCompany;

    @BeforeEach
    void setUp() {
        Category rawCategory = Category.builder()
                .name("Roads")
                .description("Road related issues")
                .isDeleted(false)
                .build();
        category = entityManager.persistAndFlush(rawCategory);

        User activeUser = buildUser("active-company@fixmycity.lv");
        entityManager.persistAndFlush(activeUser);

        User inactiveUser = buildUser("inactive-company@fixmycity.lv");
        entityManager.persistAndFlush(inactiveUser);

        Company rawActiveCompany = buildCompany(activeUser, "Roadworks Ltd", "REG-001", true);
        activeCompany = entityManager.persistAndFlush(rawActiveCompany);
    }

    private User buildUser(String email) {
        return User.builder()
                .email(email)
                .password("hashedPassword")
                .fullName("Company Contact")
                .role(Role.COMPANY)
                .enabled(true)
                .build();
    }

    private Company buildCompany(User user, String companyName, String registrationNo, boolean active) {
        return Company.builder()
                .user(user)
                .category(category)
                .companyName(companyName)
                .registrationNo(registrationNo)
                .contactEmail(companyName.toLowerCase() + "@example.com")
                .active(active)
                .build();
    }

    @Test
    void findAllByActiveTrue_shouldReturnOnlyActiveCompanies() {
        List<Company> result = companyRepository.findAllByActiveTrue();

        assertThat(result)
                .hasSize(1)
                .extracting(Company::getCompanyId)
                .containsExactly(activeCompany.getCompanyId());
    }

    @Test
    void findAllByActiveTrue_shouldReturnEmptyListWhenNoActiveCompanies() {
        companyRepository.deleteAll();

        User user = buildUser("only-inactive@fixmycity.lv");
        entityManager.persistAndFlush(user);
        entityManager.persistAndFlush(buildCompany(user, "InactiveOnly Ltd", "REG-003", false));

        List<Company> result = companyRepository.findAllByActiveTrue();

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByActiveTrue_shouldReturnEmptyListWhenNoCompaniesExist() {
        companyRepository.deleteAll();

        List<Company> result = companyRepository.findAllByActiveTrue();

        assertThat(result).isEmpty();
    }
}