package lv.acnbootcamp.fixmycity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lv.acnbootcamp.fixmycity.entity.user.User;

@Entity
@Table(name = "companies")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long companyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "registration_no", nullable = false, unique = true)
    private String registrationNo;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "contact_phone", nullable = true)
    private String contactPhone;

    @Column(name = "address", nullable = true)
    private String address;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;
}