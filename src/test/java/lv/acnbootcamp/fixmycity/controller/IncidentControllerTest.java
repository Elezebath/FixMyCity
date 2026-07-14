package lv.acnbootcamp.fixmycity.controller;

import lv.acnbootcamp.fixmycity.dto.incident.CreateIncidentRequest;
import lv.acnbootcamp.fixmycity.dto.incident.IncidentResponse;
import lv.acnbootcamp.fixmycity.entity.IncidentPriority;
import lv.acnbootcamp.fixmycity.entity.IncidentStatus;
import lv.acnbootcamp.fixmycity.exception.*;
import lv.acnbootcamp.fixmycity.security.JwtAuthenticationFilter;
import lv.acnbootcamp.fixmycity.security.UserDetailsServiceImpl;
import lv.acnbootcamp.fixmycity.service.IncidentService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IncidentService incidentService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private IncidentResponse createResponse(Long id) {

        return IncidentResponse.builder()
                .incidentId(id)
                .title("Broken Street Light")
                .description("Street light is not working")
                .locationAddress("Riga")
                .categoryName("Infrastructure")
                .categoryId(1L)
                .citizenId(10L)
                .status("NEW")
                .priority("MEDIUM")
                .build();
    }

    // GET ALL

    @Test
    @DisplayName("Should return all incidents")
    void shouldReturnAllIncidents() throws Exception {

        when(incidentService.findAll())
                .thenReturn(List.of(
                        createResponse(1L),
                        createResponse(2L)
                ));

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()")
                        .value(2));

        verify(incidentService)
                .findAll();
    }

    @Test
    @DisplayName("Should return empty incident list")
    void shouldReturnEmptyIncidentList() throws Exception {

        when(incidentService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(0));
    }

    // GET BY ID


    @Test
    @DisplayName("Should return incident by id")
    void shouldReturnIncidentById() throws Exception {

        when(incidentService.findById(1L))
                .thenReturn(createResponse(1L));

        mockMvc.perform(get("/api/incidents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incidentId")
                        .value(1))
                .andExpect(jsonPath("$.title")
                        .value("Broken Street Light"));


        verify(incidentService)
                .findById(1L);
    }

    @Test
    @DisplayName("Should return 400 when id is zero")
    void shouldRejectZeroId() throws Exception {

        mockMvc.perform(get("/api/incidents/0"))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(incidentService);
    }

    @Test
    @DisplayName("Should return 400 when id is negative")
    void shouldRejectNegativeId() throws Exception {


        mockMvc.perform(get("/api/incidents/-1"))
                .andExpect(status().isBadRequest());


        verifyNoInteractions(incidentService);
    }

    @Test
    @DisplayName("Should return 404 when incident does not exist")
    void shouldReturn404WhenIncidentMissing() throws Exception {

        when(incidentService.findById(anyLong()))
                .thenThrow(new IncidentNotFoundException("Incident not found"));

        mockMvc.perform(get("/api/incidents/5"))
                .andExpect(status().isNotFound());
    }

    // STATUS


    @Test
    @DisplayName("Should find incidents by status")
    void shouldFindByStatus() throws Exception {

        when(incidentService.findAllByStatus(IncidentStatus.NEW))
                .thenReturn(List.of(createResponse(1L)));

        mockMvc.perform(get("/api/incidents/status/NEW"))
                .andExpect(status().isOk());

        verify(incidentService)
                .findAllByStatus(
                        IncidentStatus.NEW);
    }

    @Test
    @DisplayName("Should reject invalid status")
    void shouldRejectInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/incidents/status/INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    // PRIORITY


    @Test
    @DisplayName("Should find incidents by priority")
    void shouldFindByPriority() throws Exception {

        when(incidentService.findAllByPriority(
                IncidentPriority.HIGH))
                .thenReturn(List.of(createResponse(1L)));


        mockMvc.perform(get("/api/incidents/priority/HIGH"))
                .andExpect(status().isOk());

        verify(incidentService).findAllByPriority(
                IncidentPriority.HIGH);
    }

    @Test
    @DisplayName("Should reject invalid priority")
    void shouldRejectInvalidPriority() throws Exception {
        mockMvc.perform(get("/api/incidents/priority/INVALID"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }
}