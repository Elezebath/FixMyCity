package lv.acnbootcamp.fixmycity.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lv.acnbootcamp.fixmycity.dto.company.CompanyResponse;
import lv.acnbootcamp.fixmycity.repository.CompanyRepository;
import lv.acnbootcamp.fixmycity.service.CompanyService;
import org.springframework.stereotype.Service;
import lv.acnbootcamp.fixmycity.dto.company.CompanyUpdateRequest;
import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.entity.Company;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyNotFoundException;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public List<CompanyResponse> findAll() {
        return companyRepository.findAllByActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Transactional
    @Override
    public CompanyResponse updateCompany(Long companyId, CompanyUpdateRequest request) {

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(
                "Company with id " + companyId + " not found."));

        if (companyRepository.existsByCompanyNameIgnoreCaseAndCompanyIdNot(
                request.getCompanyName(), companyId)) {
            throw new CompanyAlreadyExistsException(
                    "Company '" + request.getCompanyName() + "' already exists.");
        }

        if (companyRepository.existsByRegistrationNoAndCompanyIdNot(
                request.getRegistrationNo(), companyId)) {
            throw new CompanyAlreadyExistsException(
                    "Company with registration number '" +
                            request.getRegistrationNo() +
                            "' already exists.");
        }

        Category category = categoryRepository
                .findByCategoryIdAndIsDeletedFalse(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category with id " + request.getCategoryId() + " not found."));

        company.setCompanyName(request.getCompanyName());
        company.setRegistrationNo(request.getRegistrationNo());
        company.setCategory(category);
        company.setContactEmail(request.getContactEmail());
        company.setContactPhone(request.getContactPhone());
        company.setAddress(request.getAddress());

        Company saved = companyRepository.save(company);

        return mapToResponse(saved);
    }

    private CompanyResponse mapToResponse(Company company) {
        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .contactEmail(company.getContactEmail())
                .active(company.getActive())
                .build();
    }
}