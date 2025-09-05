package com.anme.GRC_PV.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.anme.GRC_PV.Entity.Notification;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.dto.NotificationDTO;

import com.anme.GRC_PV.security.CurrentUserService;

import lombok.RequiredArgsConstructor;

import com.anme.GRC_PV.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUserService;

    // Récupérer toutes les notifications pour l'utilisateur connecté
    @GetMapping
    public List<NotificationDTO> getNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        user currentUser = currentUserService.getCurrentUser(userDetails);
        List<Notification> notifications = notificationService.findByRecipient(currentUser);
        return notifications.stream()
                .map(NotificationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Marquer une notification comme lue
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        user currentUser = currentUserService.getCurrentUser(userDetails);
        boolean success = notificationService.markAsRead(id, currentUser);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // si notif ne appartient pas à user
        }
    }
}
