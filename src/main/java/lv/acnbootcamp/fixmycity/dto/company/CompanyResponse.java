package lv.acnbootcamp.fixmycity.dto.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyResponse {

    private Long companyId;
    private String companyName;
    private String registrationNo;
    private Long categoryId;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private Boolean active;
}