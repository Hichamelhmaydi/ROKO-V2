package com.example.roko.controller;

import com.example.roko.controller.VoyageController;
import com.example.roko.dto.response.VoyageDTO;
import com.example.roko.exception.BusinessException;
import com.example.roko.exception.GlobalExceptionHandler;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.security.CustomUserDetailsService;
import com.example.roko.security.jwt.JwtAuthenticationEntryPoint;
import com.example.roko.security.jwt.JwtAuthenticationFilter;
import com.example.roko.service.VoyageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VoyageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser(roles = "ADMIN")
class VoyageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VoyageService voyageService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getAllVoyages_shouldReturnOk() throws Exception {
        VoyageDTO v1 = buildVoyageDto(1L, "Marrakech");
        VoyageDTO v2 = buildVoyageDto(2L, "Agadir");

        Mockito.when(voyageService.getAllVoyages()).thenReturn(List.of(v1, v2));

        mockMvc.perform(get("/api/voyages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].destination").value("Agadir"));
    }

    @Test
    void getVoyageById_shouldReturnOk() throws Exception {
        VoyageDTO voyage = buildVoyageDto(10L, "Fes");
        Mockito.when(voyageService.getVoyageById(10L)).thenReturn(voyage);

        mockMvc.perform(get("/api/voyages/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.destination").value("Fes"));
    }

    @Test
    void createVoyage_shouldReturnCreated_whenPayloadIsValid() throws Exception {
        VoyageDTO request = buildValidCreateRequest();
        VoyageDTO created = buildVoyageDto(100L, "Chefchaouen");

        Mockito.when(voyageService.createVoyage(any(VoyageDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/voyages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void updateVoyage_shouldReturnOk_whenPayloadIsValid() throws Exception {
        VoyageDTO request = buildValidCreateRequest();
        VoyageDTO updated = buildVoyageDto(22L, "Merzouga");

        Mockito.when(voyageService.updateVoyage(eq(22L), any(VoyageDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/api/voyages/22")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(22))
                .andExpect(jsonPath("$.destination").value("Merzouga"));
    }

    @Test
    void deleteVoyage_shouldReturnNoContent_whenDeletionSucceeds() throws Exception {
        doNothing().when(voyageService).deleteVoyage(8L);

        mockMvc.perform(delete("/api/voyages/8"))
                .andExpect(status().isNoContent());

        verify(voyageService, times(1)).deleteVoyage(8L);
    }

    @Test
    void deleteVoyage_shouldReturnNotFound_whenVoyageDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Voyage non trouvé avec l'ID: 999"))
                .when(voyageService).deleteVoyage(999L);

        mockMvc.perform(delete("/api/voyages/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteVoyage_shouldReturnBadRequest_whenBusinessConstraintFails() throws Exception {
        doThrow(new BusinessException("Suppression impossible: ce voyage est encore lie a d'autres donnees."))
                .when(voyageService).deleteVoyage(9L);

        mockMvc.perform(delete("/api/voyages/9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createVoyage_shouldReturnBadRequest_whenNomMissing() throws Exception {
        VoyageDTO invalid = buildValidCreateRequest();
        invalid.setNom(" ");

        mockMvc.perform(post("/api/voyages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nom").value("Le nom du voyage est obligatoire"));
    }

    @Test
    void createVoyage_shouldReturnBadRequest_whenDateDepartIsInPast() throws Exception {
        VoyageDTO invalid = buildValidCreateRequest();
        invalid.setDateDepart(LocalDate.now().minusDays(1).toString());

        mockMvc.perform(post("/api/voyages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dateDepartValid")
                        .value("La date de départ doit être aujourd'hui ou dans le futur"));
    }

    @Test
    void createVoyage_shouldReturnBadRequest_whenDateRetourBeforeDateDepart() throws Exception {
        VoyageDTO invalid = buildValidCreateRequest();
        invalid.setDateDepart(LocalDate.now().plusDays(5).toString());
        invalid.setDateRetour(LocalDate.now().plusDays(4).toString());

        mockMvc.perform(post("/api/voyages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dateRangeValid")
                        .value("La date de retour doit être après la date de départ"));
    }

    @Test
    void createVoyage_shouldReturnBadRequest_whenPrixBaseInvalid() throws Exception {
        VoyageDTO invalid = buildValidCreateRequest();
        invalid.setPrixBase(BigDecimal.ZERO);

        mockMvc.perform(post("/api/voyages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.prixBase").value("Le prix de base doit être supérieur à 0"));
    }

    private VoyageDTO buildValidCreateRequest() {
        VoyageDTO dto = new VoyageDTO();
        dto.setNom("Voyage Test");
        dto.setDescription("Description test");
        dto.setDestination("Meknes");
        dto.setDateDepart(LocalDate.now().plusDays(10).toString());
        dto.setDateRetour(LocalDate.now().plusDays(12).toString());
        dto.setPrixBase(new BigDecimal("1500.00"));
        dto.setPrixInitial(new BigDecimal("1500.00"));
        dto.setItineraire("Jour 1 - Jour 2");
        dto.setCover("uploads/cover.jpg");
        return dto;
    }

    private VoyageDTO buildVoyageDto(Long id, String destination) {
        VoyageDTO dto = buildValidCreateRequest();
        dto.setId(id);
        dto.setDestination(destination);
        return dto;
    }
}
