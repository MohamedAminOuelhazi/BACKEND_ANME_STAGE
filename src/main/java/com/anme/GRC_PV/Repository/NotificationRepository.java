package com.anme.GRC_PV.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anme.GRC_PV.Entity.Notification;
import com.anme.GRC_PV.Entity.user;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientAndReadIsFalseOrderByCreatedAtDesc(user recipient);

}
