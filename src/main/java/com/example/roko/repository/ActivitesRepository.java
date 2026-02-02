package com.example.roko.repository;

import com.example.roko.entity.Activites;
import com.example.roko.entity.Voyages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface ActivitesRepository extends JpaRepository<Activites, Long> {
    List<Activites> findAllByActivites_Id(Long id);
    List<Activites> findByNom(String nom);
    List<Activites> findByNomContainingIgnoreCase(String nom);
    Boolean existsByNomAndVoyage_Id(String nom, long  voyage_id);
    @Query("SELECT COUNT(a) FROM Activites a WHERE a.voyage.id = :voyageId")
    long countByVoyageId(@Param("voyageId") Long voyageId);
}
