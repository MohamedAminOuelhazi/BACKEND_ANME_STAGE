package com.anme.GRC_PV.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.anme.GRC_PV.Entity.Direction_technique;
import com.anme.GRC_PV.Entity.Notification;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.NotificationRepository;
import com.anme.GRC_PV.dto.NotificationDTO;

import com.anme.GRC_PV.Entity.Reunion;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
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

        // Méthodes spécifiques au workflow
        public void notifyMeetingCreator(Reunion reunion, String action) {
                String title = "Réunion " + action;
                String validateurs = reunion.getValidateurs().stream()
                                .map(Direction_technique::getUsername)
                                .reduce((a, b) -> a + ", " + b).orElse("");
                String message = String.format(
                                "Votre réunion '%s' a été %s par %s",
                                reunion.getSujet(),
                                action.toLowerCase(),
                                validateurs);
                createAndSendNotification(reunion.getCreateur(), title, message);
        }

        public void notifyTechnicalCommission(Reunion reunion) {
                String title = "Nouvelle réunion programmée";
                String message = String.format(
                                "Vous êtes invité à la réunion '%s' le %s",
                                reunion.getSujet(),
                                reunion.getDateProposee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

                reunion.getParticipants()
                                .forEach(participant -> createAndSendNotification(participant, title, message));
        }

        public void notifierCreateurReunionValidee(Reunion reunion) {
                user createur = reunion.getCreateur();
                String validateurs = reunion.getValidateurs().stream()
                                .map(Direction_technique::getUsername)
                                .reduce((a, b) -> a + ", " + b).orElse("");
                String titre = "Réunion validée : " + reunion.getSujet();
                String message = String.format(
                                "Votre réunion '%s' prévue le %s a été validée par %s",
                                reunion.getSujet(),
                                reunion.getDateProposee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                validateurs);
                createAndSendNotification(createur, titre, message);
        }

        public void notifierParticipants(Reunion reunion) {
                String titre = "Invitation à réunion : " + reunion.getSujet();
                String message = String.format(
                                "Vous êtes invité à la réunion '%s' organisée par %s, prévue le %s",
                                reunion.getSujet(),
                                reunion.getCreateur().getUsername(),
                                reunion.getDateProposee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

                // Notifie chaque participant
                reunion.getParticipants().forEach(participant -> {
                        createAndSendNotification(
                                        participant,
                                        titre,
                                        message);
                });
        }

        /**
         * Notifie le créateur du rejet de sa réunion
         */
        @Async
        public void notifierRejetReunion(Reunion reunion, user validateur, String motif) {

                user createur = reunion.getCreateur();

                String titre = "Réunion rejetée : " + reunion.getSujet();
                String message = String.format(
                                """
                                                Votre réunion "%s" prévue le %s a été rejetée.

                                                Validateur : %s
                                                Motif : %s

                                                Vous pouvez modifier la demande et la soumettre à nouveau.
                                                """,
                                reunion.getSujet(),
                                reunion.getDateProposee().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                validateur.getUsername(),
                                motif);

                // 1. Sauvegarde en base
                Notification notification = new Notification();
                notification.setRecipient(createur);
                notification.setTitle(titre);
                notification.setMessage(message);
                notification.setRead(false);
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);

                // 2. Envoi WebSocket
                websocket.convertAndSendToUser(
                                createur.getUsername(),
                                "/queue/notifications",
                                new NotificationDTO(
                                                notification.getId(),
                                                notification.getTitle(),
                                                notification.getMessage(),
                                                notification.getCreatedAt(),
                                                notification.isRead()));

        }

        /**
         * Notifie tous les acteurs lorsqu'une réunion est confirmée (état VALIDATED)
         */
        @Async
        public void notifierConfirmationFinale(Reunion reunion) {
                // 1. Notification au créateur (FTE)
                notifierCreateurConfirmation(reunion);

                // 2. Notification à la direction technique
                notifierDirectionTechnique(reunion);

                // 3. Notification aux participants
                notifierParticipantsConfirmation(reunion);
        }

        // --- Méthodes privées pour modularité ---

        private void notifierCreateurConfirmation(Reunion reunion) {
                String titre = "Réunion confirmée : " + reunion.getSujet();
                String message = String.format(
                                "Votre réunion '%s' du %s est confirmée et planifiée.",
                                reunion.getSujet(),
                                formatDate(reunion.getDateProposee()));
                createAndSendNotification(reunion.getCreateur(), titre, message);
        }

        private void notifierDirectionTechnique(Reunion reunion) {
                if (reunion.getValidateurs() != null && !reunion.getValidateurs().isEmpty()) {
                        String titre = "Réunion confirmée : " + reunion.getSujet();
                        String message = String.format(
                                        "La réunion '%s' que vous avez validée est maintenant confirmée pour le %s",
                                        reunion.getSujet(),
                                        formatDate(reunion.getDateProposee()));
                        reunion.getValidateurs()
                                        .forEach(direction -> createAndSendNotification(direction, titre, message));
                }
        }

        private void notifierParticipantsConfirmation(Reunion reunion) {
                String titre = "Réunion confirmée : " + reunion.getSujet();
                String message = String.format(
                                """
                                                La réunion '%s' a été confirmée.
                                                Date : %s
                                                Organisateur : %s
                                                """,
                                reunion.getSujet(),
                                formatDate(reunion.getDateProposee()),
                                reunion.getCreateur().getUsername());
                reunion.getParticipants()
                                .forEach(participant -> createAndSendNotification(participant, titre, message));
        }

        // Méthode helper pour le formatage
        private String formatDate(LocalDateTime date) {
                return date.format(DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy 'à' HH'h'mm", Locale.FRENCH));
        }

        /**
         * Notifie tous les acteurs concernés par l'annulation d'une réunion
         */
        @Async
        public void notifierAnnulation(Reunion reunion) {
                // 1. Notification au créateur (FTE)
                notifierCreateurAnnulation(reunion);

                // 2. Notification à la direction technique (si déjà validée)
                if (reunion.getValidateurs() != null && !reunion.getValidateurs().isEmpty()) {
                        notifierDirectionAnnulation(reunion);
                }

                // 3. Notification aux participants (si existants)
                if (!reunion.getParticipants().isEmpty()) {
                        notifierParticipantsAnnulation(reunion);
                }
        }

        // --- Méthodes privées ---

        private void notifierCreateurAnnulation(Reunion reunion) {
                String titre = "Réunion annulée : " + reunion.getSujet();
                String message = String.format(
                                """
                                                Vous avez annulé la réunion :
                                                Sujet : %s
                                                Date prévue : %s
                                                """,
                                reunion.getSujet(),
                                formatDate(reunion.getDateProposee()));
                createAndSendNotification(reunion.getCreateur(), titre, message);
        }

        private void notifierDirectionAnnulation(Reunion reunion) {
                String titre = "Réunion annulée : " + reunion.getSujet();
                String message = String.format(
                                """
                                                La réunion que vous aviez validée a été annulée :
                                                Sujet : %s
                                                Organisateur : %s
                                                Date prévue : %s
                                                """,
                                reunion.getSujet(),
                                reunion.getCreateur().getUsername(),
                                formatDate(reunion.getDateProposee()));
                reunion.getValidateurs().forEach(direction -> createAndSendNotification(direction, titre, message));
        }

        private void notifierParticipantsAnnulation(Reunion reunion) {
                String titre = "Réunion annulée : " + reunion.getSujet();
                String message = String.format(
                                """
                                                La réunion à laquelle vous étiez invité a été annulée :
                                                Sujet : %s
                                                Organisateur : %s
                                                Date prévue : %s
                                                """,
                                reunion.getSujet(),
                                reunion.getCreateur().getUsername(),
                                formatDate(reunion.getDateProposee()));

                reunion.getParticipants()
                                .forEach(participant -> createAndSendNotification(participant, titre, message));
        }

        public List<Notification> findByRecipient(user recipient) {
                return notificationRepository.findByRecipientAndReadIsFalseOrderByCreatedAtDesc(recipient);
        }

        /**
         * Marque une notification comme lue uniquement si elle appartient à
         * l'utilisateur
         */
        public boolean markAsRead(Long notificationId, user recipient) {
                Optional<Notification> notifOpt = notificationRepository.findById(notificationId);
                if (notifOpt.isPresent()) {
                        Notification notif = notifOpt.get();
                        // Comparaison avec == car getId() retourne un long primitif
                        if (notif.getRecipient().getId() == recipient.getId()) {
                                notif.setRead(true);
                                notificationRepository.save(notif);
                                return true;
                        }
                }
                return false;
        }

}
