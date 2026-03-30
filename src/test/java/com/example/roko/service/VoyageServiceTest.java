package com.example.roko.service;

import com.example.roko.dto.response.VoyageDTO;
import com.example.roko.entity.Voyages;
import com.example.roko.enums.VoyageStatus;
import com.example.roko.exception.BusinessException;
import com.example.roko.exception.ResourceNotFoundException;
import com.example.roko.mapper.VoyageMapper;
import com.example.roko.repository.ActiviteRepository;
import com.example.roko.repository.PaymentRepository;
import com.example.roko.repository.ReservationRepository;
import com.example.roko.repository.VoyageRepository;
import com.example.roko.service.VoyageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoyageServiceTest {

    @Mock
    private VoyageRepository voyageRepository;

    @Mock
    private VoyageMapper voyageMapper;

    @Mock
    private ActiviteRepository activiteRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private VoyageService voyageService;

    @Test
    void createVoyage_shouldSetDisponibleAndPrixInitialFromPrixBase() {
        VoyageDTO input = new VoyageDTO();
        input.setPrixBase(new BigDecimal("1200.00"));

        Voyages entity = new Voyages();
        entity.setPrixBase(new BigDecimal("1200.00"));

        Voyages saved = new Voyages();
        saved.setId(1L);
        saved.setPrixBase(new BigDecimal("1200.00"));
        saved.setPrixInitial(new BigDecimal("1200.00"));
        saved.setStatut(VoyageStatus.DISPONIBLE);

        VoyageDTO output = new VoyageDTO();
        output.setId(1L);
        output.setPrixBase(new BigDecimal("1200.00"));
        output.setPrixInitial(new BigDecimal("1200.00"));
        output.setStatut("DISPONIBLE");

        when(voyageMapper.toEntity(input)).thenReturn(entity);
        when(voyageRepository.save(entity)).thenReturn(saved);
        when(voyageMapper.toDTO(saved)).thenReturn(output);

        VoyageDTO result = voyageService.createVoyage(input);

        assertNotNull(result);
        assertEquals(new BigDecimal("1200.00"), entity.getPrixInitial());
        assertEquals(VoyageStatus.DISPONIBLE, entity.getStatut());
        assertEquals(1L, result.getId());
        verify(voyageRepository, times(1)).save(entity);
    }

    @Test
    void updateVoyage_shouldUpdateFieldsAndAlignPrices() {
        Long id = 7L;

        Voyages existing = new Voyages();
        existing.setId(id);
        existing.setNom("Ancien nom");
        existing.setPrixBase(new BigDecimal("1000.00"));
        existing.setPrixInitial(new BigDecimal("1000.00"));

        VoyageDTO update = new VoyageDTO();
        update.setNom("Nouveau nom");
        update.setDescription("Desc");
        update.setDestination("Tetouan");
        update.setDateDepart("2030-01-10");
        update.setDateRetour("2030-01-15");
        update.setCover("uploads/new-cover.jpg");
        update.setItineraire("Jour 1 - Jour 2");
        update.setPrixBase(new BigDecimal("1800.00"));
        update.setStatut("COMPLET");
        update.setPhotos(List.of("uploads/p1.jpg", "uploads/p2.jpg"));

        Voyages saved = new Voyages();
        saved.setId(id);
        saved.setNom("Nouveau nom");
        saved.setPrixBase(new BigDecimal("1800.00"));
        saved.setPrixInitial(new BigDecimal("1800.00"));
        saved.setStatut(VoyageStatus.COMPLET);

        VoyageDTO output = new VoyageDTO();
        output.setId(id);
        output.setNom("Nouveau nom");
        output.setPrixBase(new BigDecimal("1800.00"));
        output.setPrixInitial(new BigDecimal("1800.00"));
        output.setStatut("COMPLET");

        when(voyageRepository.findById(id)).thenReturn(Optional.of(existing));
        when(voyageRepository.save(existing)).thenReturn(saved);
        when(voyageMapper.toDTO(saved)).thenReturn(output);

        VoyageDTO result = voyageService.updateVoyage(id, update);

        assertEquals("Nouveau nom", existing.getNom());
        assertEquals(new BigDecimal("1800.00"), existing.getPrixBase());
        assertEquals(new BigDecimal("1800.00"), existing.getPrixInitial());
        assertEquals(VoyageStatus.COMPLET, existing.getStatut());
        assertEquals(2, existing.getPhotos().size());
        assertEquals("Nouveau nom", result.getNom());
    }

    @Test
    void deleteVoyage_shouldThrowResourceNotFound_whenVoyageDoesNotExist() {
        Long id = 404L;
        when(voyageRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> voyageService.deleteVoyage(id));

        verify(voyageRepository, never()).delete(any(Voyages.class));
    }

    @Test
    void deleteVoyage_shouldDeleteDependenciesAndVoyage_whenNoIntegrityError() {
        Long id = 8L;
        Voyages existing = new Voyages();
        existing.setId(id);

        List<Long> reservationIds = List.of(11L, 12L);

        when(voyageRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.findIdsByVoyageId(id)).thenReturn(reservationIds);

        voyageService.deleteVoyage(id);

        verify(reservationRepository, times(1)).deleteReservationActivitiesByReservationIds(reservationIds);
        verify(paymentRepository, times(1)).deleteByReservationIds(reservationIds);
        verify(reservationRepository, times(1)).deleteByVoyageId(id);
        verify(activiteRepository, times(1)).deleteByVoyageId(id);
        verify(voyageRepository, times(1)).delete(existing);
    }

    @Test
    void deleteVoyage_shouldSkipReservationDependentDeletion_whenNoReservations() {
        Long id = 9L;
        Voyages existing = new Voyages();
        existing.setId(id);

        when(voyageRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.findIdsByVoyageId(id)).thenReturn(List.of());

        voyageService.deleteVoyage(id);

        verify(reservationRepository, never()).deleteReservationActivitiesByReservationIds(any());
        verify(paymentRepository, never()).deleteByReservationIds(any());
        verify(reservationRepository, times(1)).deleteByVoyageId(id);
        verify(activiteRepository, times(1)).deleteByVoyageId(id);
        verify(voyageRepository, times(1)).delete(existing);
    }

    @Test
    void deleteVoyage_shouldThrowBusinessException_whenIntegrityViolationOccurs() {
        Long id = 10L;
        Voyages existing = new Voyages();
        existing.setId(id);

        when(voyageRepository.findById(id)).thenReturn(Optional.of(existing));
        when(reservationRepository.findIdsByVoyageId(id)).thenReturn(List.of(1L));
        doThrow(new DataIntegrityViolationException("FK violation"))
                .when(voyageRepository).delete(existing);

        BusinessException ex = assertThrows(BusinessException.class, () -> voyageService.deleteVoyage(id));
        assertEquals("Suppression impossible: ce voyage est encore lie a d'autres donnees.", ex.getMessage());
    }

    @Test
    void countVoyagesDisponibles_shouldDelegateToRepository() {
        when(voyageRepository.countByStatut(VoyageStatus.DISPONIBLE)).thenReturn(5L);

        long result = voyageService.countVoyagesDisponibles();

        assertEquals(5L, result);
        verify(voyageRepository, times(1)).countByStatut(eq(VoyageStatus.DISPONIBLE));
    }
}
