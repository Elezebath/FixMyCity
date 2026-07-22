package lv.acnbootcamp.fixmycity.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lv.acnbootcamp.fixmycity.dto.company.CompanyResponse;
import lv.acnbootcamp.fixmycity.dto.company.CompanyUpdateRequest;
import lv.acnbootcamp.fixmycity.service.CompanyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public List<CompanyResponse> findAll() {
        return companyService.findAll();
    }

    @PutMapping("/{companyId}")
    public CompanyResponse updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyUpdateRequest request) {
        return companyService.updateCompany(companyId, request);
    }

}