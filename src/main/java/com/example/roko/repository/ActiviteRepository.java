package com.example.roko.repository;

import com.example.roko.entity.Activites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiviteRepository extends JpaRepository<Activites, Long> {
    @Query("SELECT a FROM Activites a WHERE a.voyage.id = :voyageId")
    List<Activites> findByVoyageId(@Param("voyageId") Long voyageId);

    List<Activites> findByNom(String nom);

    @Query("SELECT a FROM Activites a WHERE LOWER(a.nom) LIKE LOWER(CONCAT('%', :nom, '%'))")
    List<Activites> searchByNom(@Param("nom") String nom);


    boolean existsByNomAndVoyageId(String nom, Long voyageId);


    @Query("SELECT COUNT(a) FROM Activites a WHERE a.voyage.id = :voyageId")
    long countByVoyageId(@Param("voyageId") Long voyageId);


    @Query("SELECT DISTINCT a FROM Activites a LEFT JOIN FETCH a.reservations WHERE a.id = :id")
    Optional<Activites> findByIdWithReservations(@Param("id") Long id);


    @Query("SELECT DISTINCT a FROM Activites a LEFT JOIN FETCH a.reservations WHERE a.voyage.id = :voyageId")
    List<Activites> findByVoyageIdWithReservations(@Param("voyageId") Long voyageId);


    @Query("SELECT a FROM Activites a WHERE LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Activites> searchByDescription(@Param("keyword") String keyword);

    @Query("SELECT a FROM Activites a LEFT JOIN a.reservations r GROUP BY a.id ORDER BY COUNT(r) DESC")
    List<Activites> findMostPopularActivites();
}