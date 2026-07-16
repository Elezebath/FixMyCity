package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.incident.AssignIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.dto.incident.ResolveIncidentRequest;
import lv.acnbootcamp.fixmycity.entity.*;
import lv.acnbootcamp.fixmycity.exception.*;
import lv.acnbootcamp.fixmycity.exception.incident.IncidentNotFoundException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidIncidentException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyNotFoundException;
import lv.acnbootcamp.fixmycity.exception.user.UserNotFoundException;
import lv.acnbootcamp.fixmycity.mapper.IncidentMapper;
import lv.acnbootcamp.fixmycity.repository.*;
import lv.acnbootcamp.fixmycity.service.impl.IncidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IncidentMapper incidentMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private Incident incident;
    private Company company;
    private User companyUser;

    @BeforeEach
    void setUp() {
        company = Company.builder()
                .companyId(1L)
                .companyName("Fixit LLC")
                .build();

        companyUser = new User();
        companyUser.setCompany(company);

        incident = Incident.builder()
                .incidentId(10L)
                .title("Broken light")
                .status(IncidentStatus.NEW)
                .softDeleted(false)
                .build();
    }

    @Nested
    class AssignToCompany {

        @Test
        void assignsIncidentAndSetsStatusAssigned() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(1L)
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));
            when(incidentRepository.save(any(Incident.class)))
                    .thenReturn(incident);
            when(incidentMapper.toResponse(incident))
                    .thenReturn(new IncidentResponse());

            IncidentResponse response = incidentService.assignToCompany(10L, request);

            assertThat(response).isNotNull();
            assertThat(incident.getStatus()).isEqualTo(IncidentStatus.ASSIGNED);
            assertThat(incident.getAssignedCompany()).isEqualTo(company);
            verify(incidentRepository).save(incident);
        }

        @Test
        void throwsWhenIncidentNotFound() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(1L)
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request))
                    .isInstanceOf(IncidentNotFoundException.class);

            verifyNoInteractions(companyRepository);
        }

        @Test
        void throwsWhenCompanyNotFound() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(99L)
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(companyRepository.findById(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request))
                    .isInstanceOf(CompanyNotFoundException.class);

            verify(incidentRepository, never()).save(any());
        }

        @Test
        void throwsWhenCompanyIdMissing() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(null)
                    .build();

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request))
                    .isInstanceOf(InvalidIncidentException.class);

            verifyNoInteractions(incidentRepository);
        }

        @Test
        void throwsWhenIdIsInvalid() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(1L)
                    .build();

            assertThatThrownBy(() -> incidentService.assignToCompany(0L, request))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(incidentRepository);
        }
    }

    @Nested
    class ResolveByCompany {

        @Test
        void resolvesIncidentWhenUserBelongsToAssignedCompany() {
            incident.setAssignedCompany(company);
            incident.setStatus(IncidentStatus.ASSIGNED);

            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Fixed the street light")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("worker@fixit.com"))
                    .thenReturn(Optional.of(companyUser));
            when(incidentMapper.toResponse(incident))
                    .thenReturn(new IncidentResponse());

            IncidentResponse response = incidentService.resolveByCompany(10L, request, "worker@fixit.com");

            assertThat(response).isNotNull();
            assertThat(incident.getStatus()).isEqualTo(IncidentStatus.RESOLVED);
            assertThat(incident.getResolvedAt()).isNotNull();
            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        void throwsWhenCommentIsBlank() {
            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("   ")
                    .build();

            assertThatThrownBy(() -> incidentService.resolveByCompany(10L, request, "worker@fixit.com"))
                    .isInstanceOf(InvalidIncidentException.class);

            verifyNoInteractions(incidentRepository);
        }

        @Test
        void throwsWhenIncidentNotFound() {
            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Fixed it")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentService.resolveByCompany(10L, request, "worker@fixit.com"))
                    .isInstanceOf(IncidentNotFoundException.class);
        }

        @Test
        void throwsWhenUserNotFound() {
            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Fixed it")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("ghost@nowhere.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> incidentService.resolveByCompany(10L, request, "ghost@nowhere.com"))
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void throwsWhenIncidentHasNoAssignedCompany() {
            incident.setAssignedCompany(null);

            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Fixed it")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("worker@fixit.com"))
                    .thenReturn(Optional.of(companyUser));

            assertThatThrownBy(() -> incidentService.resolveByCompany(10L, request, "worker@fixit.com"))
                    .isInstanceOf(InvalidIncidentException.class)
                    .hasMessageContaining("Only the assigned company");

            verify(incidentRepository, never()).save(any());
        }

        @Test
        void throwsWhenUserBelongsToDifferentCompany() {
            Company otherCompany = Company.builder().companyId(2L).companyName("Other Co").build();
            incident.setAssignedCompany(company);

            User otherCompanyUser = new User();
            otherCompanyUser.setCompany(otherCompany);

            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Fixed it")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("other@company.com"))
                    .thenReturn(Optional.of(otherCompanyUser));

            assertThatThrownBy(() -> incidentService.resolveByCompany(10L, request, "other@company.com"))
                    .isInstanceOf(InvalidIncidentException.class);

            verify(commentRepository, never()).save(any());
        }

        @Test
        void throwsWhenResolvingUserHasNoCompany() {
            incident.setAssignedCompany(company);

            User citizenUser = new User();
            citizenUser.setCompany(null);

            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Fixed it")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(userRepository.findByEmail("citizen@mail.com"))
                    .thenReturn(Optional.of(citizenUser));

            assertThatThrownBy(() -> incidentService.resolveByCompany(10L, request, "citizen@mail.com"))
                    .isInstanceOf(InvalidIncidentException.class);
        }
    }
}