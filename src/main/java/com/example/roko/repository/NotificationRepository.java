package com.example.roko.repository;

import com.example.roko.entity.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notifications, Long> {

    @Query("SELECT n FROM Notifications n WHERE n.user.id = :userId ORDER BY n.dateCreation DESC")
    List<Notifications> findByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) FROM Notifications n WHERE n.user.id = :userId AND n.lu = false")
    long countUnreadByUserId(@Param("userId") Long userId);
}
