package com.example.roko.repository;

import com.example.roko.entity.Activites_Voyages;
import com.example.roko.entity.ActiviteVoyageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiviteVoyageRepository extends JpaRepository<Activites_Voyages, ActiviteVoyageId> {

    @Query("SELECT av FROM Activites_Voyages av WHERE av.voyage.id = :voyageId ORDER BY av.activite.id")
    List<Activites_Voyages> findByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT av FROM Activites_Voyages av WHERE av.activite.id = :activiteId")
    List<Activites_Voyages> findByActiviteId(@Param("activiteId") Long activiteId);

    @Query("SELECT av FROM Activites_Voyages av WHERE av.activite.id = :activiteId AND av.voyage.id = :voyageId")
    Optional<Activites_Voyages> findByActiviteIdAndVoyageId(
            @Param("activiteId") Long activiteId,
            @Param("voyageId") Long voyageId);

    boolean existsByActiviteIdAndVoyageId(Long activiteId, Long voyageId);

    @Query("SELECT av FROM Activites_Voyages av WHERE 1 = 0")
    List<Activites_Voyages> findObligatoiresByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT av FROM Activites_Voyages av WHERE av.voyage.id = :voyageId ORDER BY av.activite.id")
    List<Activites_Voyages> findOptionellesByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT av FROM Activites_Voyages av WHERE av.voyage.id = :voyageId ORDER BY av.activite.id")
    List<Activites_Voyages> findDisponiblesByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT COUNT(av) FROM Activites_Voyages av WHERE av.voyage.id = :voyageId")
    long countByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT 0")
    long countObligatoiresByVoyageId(@Param("voyageId") Long voyageId);

    @Query("SELECT av FROM Activites_Voyages av WHERE 1 = 0")
    List<Activites_Voyages> findByVoyageIdAndJour(
            @Param("voyageId") Long voyageId,
            @Param("jour") String jour);

    @Modifying
    @Query("DELETE FROM Activites_Voyages av WHERE av.voyage.id = :voyageId")
    void deleteByVoyageId(@Param("voyageId") Long voyageId);

    @Modifying
    @Query("DELETE FROM Activites_Voyages av WHERE av.activite.id = :activiteId")
    void deleteByActiviteId(@Param("activiteId") Long activiteId);
}
