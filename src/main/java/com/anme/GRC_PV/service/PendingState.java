package com.anme.GRC_PV.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.anme.GRC_PV.Entity.Direction_technique;
import com.anme.GRC_PV.Entity.Commission_techniques;
import com.anme.GRC_PV.Entity.Reunion;
import com.anme.GRC_PV.Entity.ReunionStatus;
import com.anme.GRC_PV.Entity.ReunionDocument;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.ReunionRepository;
import com.anme.GRC_PV.Repository.userRepo;
import com.anme.GRC_PV.dto.ReunionValidationDTO;

import jakarta.transaction.Transactional;

@Service
public class PendingState implements ReunionState {

    @Autowired
    private ReunionRepository reunionRepository;
    @Autowired
    private userRepo userRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private FileStorageService fileStorageService;

    @Override
    @Transactional
    public void valider(Reunion reunion, user validateur, ReunionValidationDTO dto) {
        if (!(validateur instanceof Direction_technique)) {
            throw new IllegalStateException("Seul la Direction Technique peut valider");
        }

        // Vérifier si des fichiers ont été fournis
        if (dto.getFichiers() == null || dto.getFichiers().isEmpty()) {
            throw new IllegalStateException("Des fichiers sont requis pour la validation");
        }

        reunion.setStatus(ReunionStatus.SCHEDULED);
        if (reunion.getValidateurs() != null && !reunion.getValidateurs().contains((Direction_technique) validateur)) {
            reunion.getValidateurs().add((Direction_technique) validateur);
        }

        // Ajout des participants
        List<Commission_techniques> participants = userRepository.findAllById(dto.getParticipantIds())
                .stream()
                .filter(u -> u instanceof Commission_techniques)
                .map(u -> (Commission_techniques) u)
                .toList();

        reunion.getParticipants().addAll(participants);

        // Traitement des fichiers
        for (MultipartFile fichier : dto.getFichiers()) {
            ReunionDocument document = new ReunionDocument();
            document.setNomFichier(fichier.getOriginalFilename());
            document.setDateUpload(LocalDateTime.now());
            document.setReunion(reunion);
            String cheminFichier = fileStorageService.stockerFichier(fichier);
            document.setCheminFichier(cheminFichier);
            reunion.getDocuments().add(document);
        }

        reunionRepository.save(reunion);

        // Notifications WebSocket
        notificationService.notifierCreateurReunionValidee(reunion);
        notificationService.notifierParticipants(reunion);
    }

    @Override
    public void rejeter(Reunion reunion, user validateur, String motif) {
        reunion.setStatus(ReunionStatus.REJECTED);
        reunionRepository.save(reunion);

        // Notification WebSocket du rejet
        notificationService.notifierRejetReunion(reunion, validateur, motif);
    }

    @Override
    public void confirmer(Reunion reunion) {
        throw new IllegalStateException("Impossible de confirmer une réunion en attente");
    }

    @Override
    public void annuler(Reunion reunion) {
        throw new IllegalStateException("Impossible d'annuler une réunion en attente");
    }
}