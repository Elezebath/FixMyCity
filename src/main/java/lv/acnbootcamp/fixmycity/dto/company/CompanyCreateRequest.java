package lv.acnbootcamp.fixmycity.dto.company;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyCreateRequest {

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Registration number is required")
    private String registrationNo;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    private String contactEmail;

    private String contactPhone;

    private String address;
}