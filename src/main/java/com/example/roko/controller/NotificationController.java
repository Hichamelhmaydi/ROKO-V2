package com.example.roko.controller;

import com.example.roko.dto.response.NotificationDTO;
import com.example.roko.security.UserPrincipal;
import com.example.roko.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(notificationService.getMyNotifications(userId));
    }

    @GetMapping("/me/unread-count")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        long count = notificationService.countUnread(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('VOYAGEUR', 'ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        notificationService.markAsRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) authentication.getPrincipal()).getId();
        }
        throw new RuntimeException("Impossible de récupérer l'ID de l'utilisateur");
    }
}
