package com.example.roko.repository;

import com.example.roko.entity.Voyageurs;
import com.example.roko.enums.CompteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoyageurRepository extends JpaRepository<Voyageurs, Long> {

    Optional<Voyageurs> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Voyageurs> findByStatus(CompteStatus status);

    Optional<Voyageurs> findByIdNational(String idNational);

    @Query("SELECT v FROM Voyageurs v WHERE LOWER(v.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(v.prenom) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Voyageurs> searchByNomOrPrenom(@Param("search") String search);

    long countByStatus(CompteStatus status);

    long countByStatusAndBloqueFalse(CompteStatus status);

    long countByBloqueTrue();

    List<Voyageurs> findAllByStatusOrderByNomAsc(CompteStatus status);
}
