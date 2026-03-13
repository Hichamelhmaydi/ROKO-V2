package com.example.roko.repository;

import com.example.roko.entity.Avis;
import com.example.roko.enums.AvisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvisRepository extends JpaRepository<Avis, Long> {

    @Query("SELECT a FROM Avis a JOIN FETCH a.voyageur JOIN FETCH a.voyage WHERE a.voyage.id = :voyageId AND a.statut = 'VALIDE' ORDER BY a.dateCreation DESC")
    List<Avis> findValidatedByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT a FROM Avis a JOIN FETCH a.voyageur JOIN FETCH a.voyage WHERE a.voyageur.id = :voyageurId ORDER BY a.dateCreation DESC")
    List<Avis> findByVoyageurId(@Param("voyageurId") Long voyageurId);

    @Query("SELECT a FROM Avis a JOIN FETCH a.voyageur JOIN FETCH a.voyage WHERE a.statut = :statut ORDER BY a.dateCreation DESC")
    List<Avis> findByStatut(@Param("statut") AvisStatus statut);

    @Query("SELECT COUNT(a) FROM Avis a WHERE a.statut = :statut")
    long countByStatut(@Param("statut") AvisStatus statut);
}
