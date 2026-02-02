package com.example.roko.repository;

import com.example.roko.entity.Voyages;
import com.example.roko.enums.VoyageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoyageRepository extends JpaRepository<Voyages, Long> {


    List<Voyages> findByDestination(String destination);


    List<Voyages> findByDestinationContainingIgnoreCase(String destination);


    List<Voyages> findByStatut(VoyageStatus statut);


    List<Voyages> findByStatutOrderByDateDepartAsc(VoyageStatus statut);


    List<Voyages> findByNomContainingIgnoreCase(String nom);


    List<Voyages> findByDestinationAndStatut(String destination, VoyageStatus statut);


    @Query("SELECT v FROM Voyage v WHERE " +
            "LOWER(v.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.destination) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(v.itineraire) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Voyages> searchVoyages(@Param("search") String search);


    long countByStatut(VoyageStatus statut);


    List<Voyages> findAllByStatutOrderByDateDepartAsc(VoyageStatus statut);

    @Query("SELECT v FROM Voyage v WHERE v.dateDepart >= :dateDepart")
    List<Voyages> findByDateDepartAfter(@Param("dateDepart") String dateDepart);
}