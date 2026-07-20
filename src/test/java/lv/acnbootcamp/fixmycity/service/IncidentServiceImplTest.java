package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.incident.*;
import lv.acnbootcamp.fixmycity.entity.*;
import lv.acnbootcamp.fixmycity.entity.incident.*;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.exception.incident.IncidentNotFoundException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidIncidentException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidPriorityException;
import lv.acnbootcamp.fixmycity.exception.incident.InvalidStatusException;
import lv.acnbootcamp.fixmycity.exception.user.CompanyNotFoundException;
import lv.acnbootcamp.fixmycity.exception.user.UserNotFoundException;
import lv.acnbootcamp.fixmycity.mapper.IncidentMapper;
import lv.acnbootcamp.fixmycity.service.impl.IncidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import lv.acnbootcamp.fixmycity.repository.*;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Mock
    private IncidentStatusHistoryRepository incidentStatusHistoryRepository;

    private Incident incident;
    private IncidentResponse response;
    private User citizen;
    private Category category;
    private Company company;
    private User companyUser;


    @BeforeEach
    void setUp() {
        company = Company.builder()
                .companyId(1L)
                .companyName("Fixit LLC")
                .build();

        companyUser = new User();
        companyUser.setId(2L);
        companyUser.setCompany(company);

        citizen = User.builder()
                .id(1L)
                .build();

        category = Category.builder()
                .categoryId(1L)
                .build();

        incident = Incident.builder()
                .incidentId(1L)
                .title("Broken street light")
                .description("Street light is not working")
                .status(IncidentStatus.NEW)
                .priority(IncidentPriority.MEDIUM)
                .citizen(citizen)
                .category(category)
                .softDeleted(false)
                .build();

        response = new IncidentResponse();
    }

    @Test
    void findAll_shouldReturnActiveIncidents() {
        when(incidentRepository.findAllBySoftDeletedFalse())
                .thenReturn(List.of(incident));

        when(incidentMapper.toResponse(incident)).
                thenReturn(response);

        List<IncidentResponse> result = incidentService.findAll();
        assertThat(result).hasSize(1);

        verify(incidentRepository)
                .findAllBySoftDeletedFalse();
        verify(incidentMapper)
                .toResponse(incident);
    }

    @Test
    void findAll_shouldReturnEmptyList_whenNoIncidents() {
        when(incidentRepository.findAllBySoftDeletedFalse())
                .thenReturn(List.of());

        List<IncidentResponse> result = incidentService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_shouldReturnIncident_whenExists() {
        when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(1L))
                .thenReturn(Optional.of(incident));
        when(incidentMapper.toResponse(incident))
                .thenReturn(response);
        IncidentResponse result = incidentService.findById(1L);

        assertThat(result)
                .isEqualTo(response);
    }

    @Test
    void findById_shouldThrowException_whenIncidentNotFound() {
        when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.findById(1L))
                .isInstanceOf(IncidentNotFoundException.class);

    }

    @Test
    void findById_shouldThrowException_whenIdNull() {
        assertThatThrownBy(() -> incidentService.findById(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findById_shouldThrowException_whenIdNegative() {
        assertThatThrownBy(() -> incidentService.findById(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findAllByStatus_shouldReturnIncidents() {
        when(incidentRepository.findAllBySoftDeletedFalseAndStatus(IncidentStatus.NEW))
                .thenReturn(List.of(incident));

        when(incidentMapper.toResponse(incident)).thenReturn(response);

        List<IncidentResponse> result = incidentService.findAllByStatus(IncidentStatus.NEW);

        assertThat(result)
                .hasSize(1);

    }

    @Test
    void findAllByStatus_shouldThrowException_whenStatusNull() {
        assertThatThrownBy(() -> incidentService.findAllByStatus(null))
                .isInstanceOf(InvalidStatusException.class);

    }

    @Test()
    void findAllByPriority_shouldReturnIncidents() {
        when(incidentRepository.findAllBySoftDeletedFalseAndPriority(IncidentPriority.HIGH))
                .thenReturn(List.of(incident));

        when(incidentMapper.toResponse(incident))
                .thenReturn(response);
        List<IncidentResponse> result = incidentService.findAllByPriority(IncidentPriority.HIGH);

        assertThat(result)
                .hasSize(1);

    }

    @Test
    void findAllByPriority_shouldThrowException_whenNull() {
        assertThatThrownBy(() -> incidentService.findAllByPriority(null))
                .isInstanceOf(InvalidPriorityException.class);

    }

    @Test
    void findAllByCitizen_shouldReturnIncidents() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(incidentRepository.findAllBySoftDeletedFalseAndCitizenId(1L))
                .thenReturn(List.of(incident));
        when(incidentMapper.toResponse(incident))
                .thenReturn(response);
        List<IncidentResponse> result = incidentService.findAllByCitizen(1L);
        assertThat(result)
                .hasSize(1);

    }

    @Test
    void findAllByCitizen_shouldThrowException_whenUserMissing() {
        when(userRepository.existsById(1L))
                .thenReturn(false);
        assertThatThrownBy(() -> incidentService.findAllByCitizen(1L))
                .isInstanceOf(UserNotFoundException.class);

    }

    // FIND BY CATEGORY
    //
    // @Test
    void findAllByCategory_shouldReturnIncidents() {
        when(categoryRepository.existsById(1L))
                .thenReturn(true);

        when(incidentRepository.findAllBySoftDeletedFalseAndCategory_CategoryId(1L))
                .thenReturn(List.of(incident));

        when(incidentMapper.toResponse(incident))
                .thenReturn(response);
        List<IncidentResponse> result = incidentService.findAllByCategory(1L);

        assertThat(result)
                .hasSize(1);

    }

    @Test
    void findAllByCategory_shouldThrowException_whenCategoryMissing() {
        when(categoryRepository.existsById(1L))
                .thenReturn(false);
        assertThatThrownBy(() -> incidentService.findAllByCategory(1L))
                .isInstanceOf(CategoryNotFoundException.class);

    }

    // CREATE INCIDENT

    @Test
    void create_shouldCreateIncidentSuccessfully() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Broken road");
        request.setDescription("Large hole");
        request.setCategoryId(1L);

        Long citizenId = 1L;

        when(userRepository.findById(citizenId)).thenReturn(Optional.of(citizen));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentMapper.toResponse(incident)).thenReturn(response);

        IncidentResponse result = incidentService.create(request, citizenId);
        assertThat(result).isEqualTo(response);
        verify(incidentRepository)
                .save(any(Incident.class));

    }

    @Test
    void create_shouldThrowException_whenRequestNull() {
        assertThatThrownBy(() -> incidentService.create(null, 1L))
                .isInstanceOf(InvalidIncidentException.class);

    }

    @Test
    void create_shouldThrowException_whenTitleMissing() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setDescription("Description");

        request.setCategoryId(1L);
        assertThatThrownBy(() -> incidentService.create(request, 1L))
                .isInstanceOf(InvalidIncidentException.class);

    }

    @Test
    void create_shouldThrowException_whenCitizenNotFound() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Title");
        request.setDescription("Description");
        request.setCategoryId(1L);

        Long citizenId = 1L;
        when(userRepository.findById(citizenId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.create(request, citizenId))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    void create_shouldThrowException_whenCategoryNotFound() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Title");
        request.setDescription("Description");
        request.setCategoryId(1L);

        Long citizenId = 1L;

        when(userRepository.findById(citizenId))
                .thenReturn(Optional.of(citizen));
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.create(request, citizenId))
                .isInstanceOf(CategoryNotFoundException.class);

    }

    @Nested
    class AssignToCompany {

        @Test
        void assignsIncidentAndSetsStatusAssigned() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(1L)
                    .build();

            User manager = User.builder().id(20L).role(Role.MANAGER).build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));
            when(companyRepository.findById(1L))
                    .thenReturn(Optional.of(company));
            when(incidentRepository.save(any(Incident.class)))
                    .thenReturn(incident);
            when(incidentMapper.toResponse(incident))
                    .thenReturn(new IncidentResponse());
            when(userRepository.findById(20L)).thenReturn(Optional.of(manager));

            IncidentResponse response = incidentService.assignToCompany(10L, request, 20L);

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

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request, 20L))
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

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request, 20L))
                    .isInstanceOf(CompanyNotFoundException.class);

            verify(incidentRepository, never()).save(any());
        }

        @Test
        void throwsWhenCompanyIdMissing() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(null)
                    .build();

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request, 20L))
                    .isInstanceOf(InvalidIncidentException.class);

            verifyNoInteractions(incidentRepository);
        }

        @Test
        void throwsWhenIdIsInvalid() {
            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(1L)
                    .build();

            assertThatThrownBy(() -> incidentService.assignToCompany(0L, request, 20L))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(incidentRepository);
        }

        @Test
        void throwsWhenIncidentAlreadyAssigned() {
            incident.setStatus(IncidentStatus.ASSIGNED);

            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(1L)
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request, 20L))
                    .isInstanceOf(InvalidIncidentException.class)
                    .hasMessageContaining("already been assigned");

            verifyNoInteractions(companyRepository);
        }

        @Test
        void throwsWhenIncidentAlreadyResolved() {
            incident.setStatus(IncidentStatus.RESOLVED);

            AssignIncidentRequest request = AssignIncidentRequest.builder()
                    .companyId(1L)
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));

            assertThatThrownBy(() -> incidentService.assignToCompany(10L, request, 20L))
                    .isInstanceOf(InvalidIncidentException.class)
                    .hasMessageContaining("Resolved incidents cannot be assigned");

            verifyNoInteractions(companyRepository);
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
            when(incidentRepository.save(any(Incident.class)))
                    .thenReturn(incident);
            when(userRepository.findById(2L))
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
            incident.setStatus(IncidentStatus.ASSIGNED);
            incident.setAssignedCompany(company);

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

            assertThatThrownBy(() -> incidentService.resolveByCompany(10L, request, "worker@fixit.com"))
                    .isInstanceOf(InvalidIncidentException.class)
                    .hasMessageContaining("Incident must be assigned before it can be resolved.");

            verify(incidentRepository, never()).save(any());
        }

        @Test
        void throwsWhenUserBelongsToDifferentCompany() {
            Company otherCompany = Company.builder().companyId(2L).companyName("Other Co").build();
            incident.setStatus(IncidentStatus.ASSIGNED);
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
            incident.setStatus(IncidentStatus.ASSIGNED);
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

        @Test
        void throwsWhenIncidentNotAssigned() {
            incident.setStatus(IncidentStatus.NEW);

            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Fixed it")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));

            assertThatThrownBy(() ->
                    incidentService.resolveByCompany(10L, request, "worker@fixit.com"))
                    .isInstanceOf(InvalidIncidentException.class)
                    .hasMessageContaining("must be assigned");

            verifyNoInteractions(userRepository);
        }
        @Test
        void throwsWhenIncidentAlreadyResolved() {
            incident.setStatus(IncidentStatus.RESOLVED);

            ResolveIncidentRequest request = ResolveIncidentRequest.builder()
                    .comment("Done")
                    .build();

            when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(10L))
                    .thenReturn(Optional.of(incident));

            assertThatThrownBy(() ->
                    incidentService.resolveByCompany(10L, request, "worker@fixit.com"))
                    .isInstanceOf(InvalidIncidentException.class)
                    .hasMessageContaining("already been resolved");
        }
    }
    @Nested
    class getStatusHistory{

    @Test
    void getStatusHistory_shouldReturnHistory() {
        IncidentStatusHistory history = IncidentStatusHistory.builder()
                .incident(incident)
                .oldStatus(IncidentStatus.NEW)
                .newStatus(IncidentStatus.ASSIGNED)
                .remarks("Assigned")
                .build();

        IncidentStatusHistoryResponse response =
                new IncidentStatusHistoryResponse();

        when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(1L))
                .thenReturn(Optional.of(incident));

        when(incidentStatusHistoryRepository
                .findAllByIncident_IncidentIdOrderByChangedAtAsc(1L))
                .thenReturn(List.of(history));

        when(incidentMapper.toStatusHistoryResponse(history, true))
                .thenReturn(response);

        List<IncidentStatusHistoryResponse> result =
                incidentService.getStatusHistory(1L, 20L, Role.MANAGER);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(response);

        verify(incidentStatusHistoryRepository)
                .findAllByIncident_IncidentIdOrderByChangedAtAsc(1L);
    }

    @Test
    void getStatusHistory_shouldThrowWhenIncidentNotFound() {

        when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                incidentService.getStatusHistory(1L, 20L, Role.MANAGER))
                .isInstanceOf(IncidentNotFoundException.class);
    }

    @Test
    void getStatusHistory_shouldHideChangedByForCitizen() {

        IncidentStatusHistory history = IncidentStatusHistory.builder()
                .incident(incident)
                .build();

        IncidentStatusHistoryResponse response =
                new IncidentStatusHistoryResponse();

        when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(1L))
                .thenReturn(Optional.of(incident));

        when(incidentStatusHistoryRepository
                .findAllByIncident_IncidentIdOrderByChangedAtAsc(1L))
                .thenReturn(List.of(history));

        when(incidentMapper.toStatusHistoryResponse(history, false))
                .thenReturn(response);

        incidentService.getStatusHistory(1L, 1L, Role.CITIZEN);

        verify(incidentMapper)
                .toStatusHistoryResponse(history, false);
    }


    @Test
    void getStatusHistory_shouldIncludeChangedByForManager() {

        IncidentStatusHistory history = IncidentStatusHistory.builder()
                .incident(incident)
                .build();

        IncidentStatusHistoryResponse response =
                new IncidentStatusHistoryResponse();

        when(incidentRepository.findByIncidentIdAndSoftDeletedFalse(1L))
                .thenReturn(Optional.of(incident));

        when(incidentStatusHistoryRepository
                .findAllByIncident_IncidentIdOrderByChangedAtAsc(1L))
                .thenReturn(List.of(history));

        when(incidentMapper.toStatusHistoryResponse(history, true))
                .thenReturn(response);

        incidentService.getStatusHistory(1L, 20L, Role.MANAGER);

        verify(incidentMapper)
                .toStatusHistoryResponse(history, true);
    }
}
}