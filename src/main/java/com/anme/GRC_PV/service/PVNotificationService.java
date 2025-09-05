package com.anme.GRC_PV.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.anme.GRC_PV.Entity.Notification;
import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.NotificationRepository;
import com.anme.GRC_PV.dto.NotificationDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PVNotificationService {
    private final SimpMessagingTemplate websocket;
    private final NotificationRepository notificationRepository;

    /**
     * Envoie une notification en temps réel et la sauvegarde en base
     */
    @Async
    public void createAndSendNotification(user recipient, String title, String message) {
        // 1. Sauvegarde en base
        Notification notif = new Notification();
        notif.setRecipient(recipient);
        notif.setTitle(title);
        notif.setMessage(message);
        notif.setRead(false);
        notif.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notif);

        // 2. Envoi WebSocket
        sendRealTimeNotification(recipient.getUsername(), notif);
    }

    /**
     * Envoi via WebSocket
     */
    private void sendRealTimeNotification(String username, Notification notification) {
        websocket.convertAndSendToUser(
                username,
                "/queue/notifications",
                new NotificationDTO(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getCreatedAt(),
                        notification.isRead()));
    }

    /**
     * Notifie le créateur du PV qu'il a été accepté
     */
    @Async
    public void notifierPVAccepte(Pv pv, user signataire) {
        String titre = "PV accepté : " + pv.getTitre();
        String message = String.format(
                "Votre PV '%s' a été accepté par %s le %s",
                pv.getTitre(),
                signataire.getUsername(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        // Notifier le créateur du PV (FTE)
        user createur = pv.getReunion().getCreateur();
        createAndSendNotification(createur, titre, message);
    }

    /**
     * Notifie le créateur du PV qu'il a été rejeté
     */
    @Async
    public void notifierPVRejete(Pv pv, user signataire, String motif) {
        String titre = "PV rejeté : " + pv.getTitre();
        String message = String.format(
                """
                        Votre PV '%s' a été rejeté par %s.

                        Motif du rejet : %s

                        Vous pouvez créer une nouvelle version du PV.
                        """,
                pv.getTitre(),
                signataire.getUsername(),
                motif);

        // Notifier le créateur du PV (FTE)
        user createur = pv.getReunion().getCreateur();
        createAndSendNotification(createur, titre, message);
    }

    /**
     * Notifie les signataires qu'un nouveau PV est disponible
     */
    @Async
    public void notifierNouveauPV(Pv pv) {
        String titre = "Nouveau PV disponible : " + pv.getTitre();
        String message = String.format(
                "Un nouveau PV '%s' est disponible pour signature.",
                pv.getTitre());

        // Notifier toutes les Directions Techniques
        pv.getReunion().getValidateurs().forEach(direction -> createAndSendNotification(direction, titre, message));

        // Notifier les membres de la Commission Technique
        pv.getReunion().getParticipants().forEach(participant -> {
            createAndSendNotification(participant, titre, message);
        });
    }

    /**
     * Notifie tous les acteurs qu'un PV a été signé par tous
     */
    @Async
    public void notifierPVCompletementSigne(Pv pv) {
        String titre = "PV complètement signé : " + pv.getTitre();
        String message = String.format(
                "Le PV '%s' a été signé par tous les acteurs et est maintenant téléchargeable.",
                pv.getTitre());

        // Notifier le créateur
        createAndSendNotification(pv.getReunion().getCreateur(), titre, message);

        // Notifier toutes les Directions Techniques
        pv.getReunion().getValidateurs().forEach(direction -> createAndSendNotification(direction, titre, message));

        // Notifier les participants
        pv.getReunion().getParticipants().forEach(participant -> {
            createAndSendNotification(participant, titre, message);
        });
    }

    /**
     * Notifie qu'une nouvelle version de PV est disponible
     */
    @Async
    public void notifierNouvelleVersionPV(Pv pv) {
        String titre = "Nouvelle version de PV : " + pv.getTitre();
        String message = String.format(
                "Une nouvelle version du PV '%s' est disponible pour signature.",
                pv.getTitre());

        // Notifier toutes les Directions Techniques
        pv.getReunion().getValidateurs().forEach(direction -> createAndSendNotification(direction, titre, message));

        // Notifier les participants
        pv.getReunion().getParticipants().forEach(participant -> {
            createAndSendNotification(participant, titre, message);
        });
    }
}