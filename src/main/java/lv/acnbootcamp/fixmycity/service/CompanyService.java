package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.company.CompanyResponse;
import lv.acnbootcamp.fixmycity.dto.company.CompanyUpdateRequest;

import java.util.List;

public interface CompanyService {

    List<CompanyResponse> findAll();
    CompanyResponse findById(Long companyId);
    CompanyResponse updateCompany(Long companyId, CompanyUpdateRequest request);
}