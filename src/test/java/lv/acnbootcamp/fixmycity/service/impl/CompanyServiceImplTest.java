package lv.acnbootcamp.fixmycity.service.impl;

import lv.acnbootcamp.fixmycity.dto.company.CompanyUpdateRequest;
import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.entity.Company;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyNotFoundException;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.CompanyRepository;
import lv.acnbootcamp.fixmycity.service.impl.CompanyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import lv.acnbootcamp.fixmycity.dto.company.CompanyResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company company;
    private Category category;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .categoryId(1L)
                .name("Roads")
                .build();

        company = Company.builder()
                .companyId(1L)
                .companyName("FixIt Co.")
                .registrationNo("REG-001")
                .contactEmail("contact@fixit.lv")
                .contactPhone("12345678")
                .address("Riga")
                .category(category)
                .active(true)
                .build();
    }
    @Nested
    class FindAll {
        @Test
        void returnsMappedResponses() {
            when(companyRepository.findAllByActiveTrue())
                    .thenReturn(List.of(company));

            List<CompanyResponse> result = companyService.findAll();

            assertThat(result).hasSize(1);

            CompanyResponse response = result.getFirst();

            assertThat(response.getCompanyId()).isEqualTo(company.getCompanyId());
            assertThat(response.getCompanyName()).isEqualTo(company.getCompanyName());
            assertThat(response.getContactEmail()).isEqualTo(company.getContactEmail());
            assertThat(response.getActive()).isEqualTo(company.getActive());
        }

        @Test
        void returnsEmptyListWhenRepositoryReturnsEmptyList() {
            when(companyRepository.findAllByActiveTrue())
                    .thenReturn(List.of());

            List<CompanyResponse> result = companyService.findAll();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class UpdateCompany {
        @Test
        void updatesSuccessfully() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));
            when(companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                    request.getCompanyName(), 1L)).thenReturn(false);
            when(companyRepository.existsByRegistrationNoAndCompanyIdNot(
                    request.getRegistrationNo(), 1L)).thenReturn(false);
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId()))
                    .thenReturn(Optional.of(category));
            when(companyRepository.save(any(Company.class))).thenAnswer(i -> i.getArgument(0));

            CompanyResponse response = companyService.updateCompany(1L, request);

            assertThat(response).isNotNull();
        }

        @Test
        void returnsUpdatedResponse() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));
            when(companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                    request.getCompanyName(), 1L)).thenReturn(false);
            when(companyRepository.existsByRegistrationNoAndCompanyIdNot(
                    request.getRegistrationNo(), 1L)).thenReturn(false);
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId()))
                    .thenReturn(Optional.of(category));
            when(companyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            CompanyResponse response = companyService.updateCompany(1L, request);

            assertThat(response.getCompanyName()).isEqualTo(request.getCompanyName());
            assertThat(response.getContactEmail()).isEqualTo(request.getContactEmail());
            assertThat(response.getActive()).isTrue();
        }

        @Test
        void updatesAllMutableFields() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));
            when(companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                    request.getCompanyName(), 1L)).thenReturn(false);
            when(companyRepository.existsByRegistrationNoAndCompanyIdNot(
                    request.getRegistrationNo(), 1L)).thenReturn(false);
            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId()))
                    .thenReturn(Optional.of(category));
            when(companyRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            companyService.updateCompany(1L, request);

            assertThat(company.getCompanyName()).isEqualTo(request.getCompanyName());
            assertThat(company.getRegistrationNo()).isEqualTo(request.getRegistrationNo());
            assertThat(company.getContactEmail()).isEqualTo(request.getContactEmail());
            assertThat(company.getContactPhone()).isEqualTo(request.getContactPhone());
            assertThat(company.getAddress()).isEqualTo(request.getAddress());
            assertThat(company.getCategory()).isEqualTo(category);
        }

        @Test
        void throwsWhenCompanyNotFound() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.updateCompany(1L, request))
                    .isInstanceOf(CompanyNotFoundException.class)
                    .hasMessage("Company with id 1 not found.");

            verify(companyRepository, never()).save(any());
        }

        @Test
        void throwsWhenCompanyNameAlreadyExists() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));

            when(companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                    request.getCompanyName(), 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> companyService.updateCompany(1L, request))
                    .isInstanceOf(CompanyAlreadyExistsException.class)
                    .hasMessage("Company '" + request.getCompanyName() + "' already exists.");

            verify(companyRepository, never()).save(any());
        }

        @Test
        void throwsWhenRegistrationNumberAlreadyExists() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));

            when(companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                    request.getCompanyName(), 1L))
                    .thenReturn(false);

            when(companyRepository.existsByRegistrationNoAndCompanyIdNot(
                    request.getRegistrationNo(), 1L))
                    .thenReturn(true);

            assertThatThrownBy(() -> companyService.updateCompany(1L, request))
                    .isInstanceOf(CompanyAlreadyExistsException.class)
                    .hasMessage("Company with registration number '" +
                            request.getRegistrationNo() +
                            "' already exists.");

            verify(companyRepository, never()).save(any());
        }

        @Test
        void throwsWhenCategoryNotFound() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));

            when(companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                    request.getCompanyName(), 1L))
                    .thenReturn(false);

            when(companyRepository.existsByRegistrationNoAndCompanyIdNot(
                    request.getRegistrationNo(), 1L))
                    .thenReturn(false);

            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.updateCompany(1L, request))
                    .isInstanceOf(CategoryNotFoundException.class)
                    .hasMessage("Category with id 1 not found.");

            verify(companyRepository, never()).save(any());
        }

        @Test
        void savesUpdatedCompany() {
            CompanyUpdateRequest request = updateRequest();

            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));

            when(companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                    request.getCompanyName(), 1L))
                    .thenReturn(false);

            when(companyRepository.existsByRegistrationNoAndCompanyIdNot(
                    request.getRegistrationNo(), 1L))
                    .thenReturn(false);

            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(request.getCategoryId()))
                    .thenReturn(Optional.of(category));

            when(companyRepository.save(any(Company.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            companyService.updateCompany(1L, request);

            verify(companyRepository).save(company);
        }
    }
    private CompanyUpdateRequest updateRequest() {
         CompanyUpdateRequest request = new CompanyUpdateRequest();
                request.setCompanyName("Updated Company");
                request.setRegistrationNo("REG-999");
                request.setContactEmail("updated@company.lv");
                request.setContactPhone("87654321");
                request.setAddress("Updated Address");
                request.setCategoryId(1L);

        return request;
    }
}
