package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.incident.CreateIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.entity.*;
import lv.acnbootcamp.fixmycity.exception.*;
import lv.acnbootcamp.fixmycity.mapper.IncidentMapper;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.IncidentRepository;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import lv.acnbootcamp.fixmycity.service.impl.IncidentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceImplTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IncidentMapper incidentMapper;

    @InjectMocks
    private IncidentServiceImpl incidentService;

    private Incident incident;
    private IncidentResponse response;
    private User citizen;
    private Category category;

    @BeforeEach
    void setUp() {
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
                .isInstanceOf( IllegalArgumentException.class);
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
                .isInstanceOf( InvalidStatusException.class);

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

    // =====================================================
    // FIND BY CATEGORY
    // =====================================================
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

    // =====================================================
    // CREATE INCIDENT
    // =====================================================
    //
    // @Test
    void create_shouldCreateIncidentSuccessfully() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Broken road");
        request.setDescription("Large hole");
        request.setCategoryId(1L);
        request.setCitizenId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(citizen));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(incidentRepository.save(any())).thenReturn(incident);
        when(incidentMapper.toResponse(incident)).thenReturn(response);

        IncidentResponse result = incidentService.create(request);
        assertThat(result).isEqualTo(response);
        verify(incidentRepository)
                .save(any(Incident.class));

    }

    @Test
    void create_shouldThrowException_whenRequestNull() {
        assertThatThrownBy(() -> incidentService.create(null))
                .isInstanceOf(InvalidIncidentException.class);

    }

    @Test
    void create_shouldThrowException_whenTitleMissing() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setDescription("Description");

        request.setCategoryId(1L);
        assertThatThrownBy(() -> incidentService.create(request))
                .isInstanceOf(InvalidIncidentException.class);

    }

    @Test
    void create_shouldThrowException_whenCitizenNotFound() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Title");
        request.setDescription("Description");
        request.setCategoryId(1L);
        request.setCitizenId(1L);
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.create(request))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    void create_shouldThrowException_whenCategoryNotFound() {
        CreateIncidentRequest request = new CreateIncidentRequest();
        request.setTitle("Title");
        request.setDescription("Description");
        request.setCategoryId(1L);
        request.setCitizenId(1L);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(citizen));
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> incidentService.create(request))
                .isInstanceOf(CategoryNotFoundException.class);

    }

}