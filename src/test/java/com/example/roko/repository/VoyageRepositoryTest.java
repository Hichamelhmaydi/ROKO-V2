package com.example.roko.repository;

import com.example.roko.entity.Voyages;
import com.example.roko.enums.VoyageStatus;
import com.example.roko.repository.VoyageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class VoyageRepositoryTest {

    @Autowired
    private VoyageRepository voyageRepository;

    @BeforeEach
    void setUp() {
        voyageRepository.deleteAll();
    }

    @Test
    void findByDestinationContainingIgnoreCase_shouldReturnMatchingVoyages() {
        voyageRepository.save(buildVoyage("Atlas Explorer", "Marrakech", "2030-01-10", VoyageStatus.DISPONIBLE));
        voyageRepository.save(buildVoyage("Nord Escape", "Tanger", "2030-01-12", VoyageStatus.DISPONIBLE));

        List<Voyages> result = voyageRepository.findByDestinationContainingIgnoreCase("marra");

        assertEquals(1, result.size());
        assertEquals("Marrakech", result.get(0).getDestination());
    }

    @Test
    void findByStatutOrderByDateDepartAsc_shouldReturnSortedResults() {
        voyageRepository.save(buildVoyage("Trip B", "Agadir", "2030-02-15", VoyageStatus.DISPONIBLE));
        voyageRepository.save(buildVoyage("Trip A", "Fes", "2030-01-05", VoyageStatus.DISPONIBLE));
        voyageRepository.save(buildVoyage("Trip C", "Oujda", "2030-03-01", VoyageStatus.COMPLET));

        List<Voyages> result = voyageRepository.findByStatutOrderByDateDepartAsc(VoyageStatus.DISPONIBLE);

        assertEquals(2, result.size());
        assertEquals("2030-01-05", result.get(0).getDateDepart());
        assertEquals("2030-02-15", result.get(1).getDateDepart());
    }

    @Test
    void searchVoyages_shouldSearchNomDestinationAndItineraire() {
        voyageRepository.save(buildVoyage("Desert Adventure", "Merzouga", "2030-04-10", VoyageStatus.DISPONIBLE));
        voyageRepository.save(buildVoyage("Ocean Breeze", "Essaouira", "2030-05-10", VoyageStatus.DISPONIBLE));

        List<Voyages> byNom = voyageRepository.searchVoyages("desert");
        List<Voyages> byDestination = voyageRepository.searchVoyages("essa");
        List<Voyages> byItineraire = voyageRepository.searchVoyages("jour 2");

        assertEquals(1, byNom.size());
        assertEquals("Desert Adventure", byNom.get(0).getNom());

        assertEquals(1, byDestination.size());
        assertEquals("Ocean Breeze", byDestination.get(0).getNom());

        assertEquals(2, byItineraire.size());
    }

    @Test
    void findByDateDepartAfter_shouldReturnVoyagesOnOrAfterDate() {
        voyageRepository.save(buildVoyage("Early", "Rabat", "2030-01-01", VoyageStatus.DISPONIBLE));
        voyageRepository.save(buildVoyage("Later", "Casablanca", "2030-01-20", VoyageStatus.DISPONIBLE));

        List<Voyages> result = voyageRepository.findByDateDepartAfter("2030-01-15");

        assertEquals(1, result.size());
        assertEquals("Later", result.get(0).getNom());
    }

    @Test
    void countByStatut_shouldReturnCorrectCount() {
        voyageRepository.save(buildVoyage("V1", "Fes", "2030-06-10", VoyageStatus.DISPONIBLE));
        voyageRepository.save(buildVoyage("V2", "Fes", "2030-06-12", VoyageStatus.DISPONIBLE));
        voyageRepository.save(buildVoyage("V3", "Fes", "2030-06-14", VoyageStatus.COMPLET));

        long countDisponible = voyageRepository.countByStatut(VoyageStatus.DISPONIBLE);
        long countComplet = voyageRepository.countByStatut(VoyageStatus.COMPLET);

        assertEquals(2, countDisponible);
        assertEquals(1, countComplet);
        assertTrue(voyageRepository.existsByDestination("Fes"));
    }

    private Voyages buildVoyage(String nom, String destination, String dateDepart, VoyageStatus statut) {
        Voyages v = new Voyages();
        v.setNom(nom);
        v.setDescription("Description " + nom);
        v.setCover("uploads/" + nom.replace(" ", "-").toLowerCase() + ".jpg");
        v.setDestination(destination);
        v.setDateDepart(dateDepart);
        v.setDateRetour("2030-12-31");
        v.setStatut(statut);
        v.setItineraire("Jour 1, Jour 2");
        v.setPrixInitial(new BigDecimal("1000.00"));
        v.setPrixBase(new BigDecimal("1200.00"));
        return v;
    }
}
