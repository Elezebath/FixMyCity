package lv.acnbootcamp.fixmycity.controller;

import lombok.RequiredArgsConstructor;
import lv.acnbootcamp.fixmycity.dto.company.CompanyResponse;
import lv.acnbootcamp.fixmycity.repository.CompanyRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;

    @GetMapping
    public List<CompanyResponse> findAll() {
        return companyRepository.findAllByActiveTrue().stream()
                .map(c -> CompanyResponse.builder()
                        .companyId(c.getCompanyId())
                        .companyName(c.getCompanyName())
                        .contactEmail(c.getContactEmail())
                        .active(c.getActive())
                        .build())
                .toList();
    }
}