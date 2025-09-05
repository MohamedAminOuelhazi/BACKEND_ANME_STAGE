package com.anme.GRC_PV.dto;

import java.time.LocalDateTime;

import com.anme.GRC_PV.Entity.Notification;

public record NotificationDTO(
                Long id,
                String title,
                String message,
                LocalDateTime createdAt,
                boolean isRead

) {
        public static NotificationDTO fromEntity(Notification notification) {
                return new NotificationDTO(
                                notification.getId(),
                                notification.getTitle(),
                                notification.getMessage(),
                                notification.getCreatedAt(),
                                notification.isRead());
        }
}