package com.example.roko.repository;

import com.example.roko.entity.User;
import com.example.roko.enums.CompteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByStatus(CompteStatus status);

    @Query("SELECT u FROM User u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<User> searchByNomOrPrenom(@Param("search") String search);

    long countByStatus(CompteStatus status);

    List<User> findAllByStatusOrderByNomAsc(CompteStatus status);

    @Query("SELECT u FROM User u WHERE TYPE(u) = :type")
    List<User> findByUserType(@Param("type") Class<? extends User> type);
}