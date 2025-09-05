package com.anme.GRC_PV.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.anme.GRC_PV.Entity.Commission_techniques;
import com.anme.GRC_PV.Entity.Direction_technique;
import com.anme.GRC_PV.Entity.Fte;
import com.anme.GRC_PV.Entity.PVVersion;
import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.Reunion;
import com.anme.GRC_PV.Entity.ReunionDocument;
import com.anme.GRC_PV.Entity.ReunionStatus;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.ReunionDocumentRepository;
import com.anme.GRC_PV.Repository.ReunionRepository;
import com.anme.GRC_PV.Repository.userRepo;
import com.anme.GRC_PV.dto.ReunionRequestDTO;
import com.anme.GRC_PV.dto.ReunionResponseDTO;
import com.anme.GRC_PV.dto.ReunionValidationDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ReunionService {

    @Autowired
    private ReunionRepository reunionRepository;
    @Autowired
    private userRepo userRepository;
    @Autowired
    private ReunionStateFactory stateFactory;

    @Autowired
    private ReunionDocumentRepository reunionDocumentRepository;

    // FileStorageService utilisé au niveau des états pour éviter les doublons
    // d'upload
    @Autowired
    private NotificationService notificationService;

    public Reunion creerReunion(ReunionRequestDTO dto, Fte createur) {
        Reunion reunion = new Reunion();
        reunion.setSujet(dto.getSujet());
        reunion.setDateProposee(dto.getDateProposee());
        reunion.setCreateur(createur);
        reunion.setStatus(ReunionStatus.PENDING);

        // Ajout des validateurs (directions techniques)
        if (dto.getValidateurIds() != null && !dto.getValidateurIds().isEmpty()) {
            List<Direction_technique> validateurs = userRepository.findAllById(dto.getValidateurIds())
                    .stream()
                    .filter(u -> u instanceof Direction_technique)
                    .map(u -> (Direction_technique) u)
                    .toList();
            reunion.getValidateurs().addAll(validateurs);
        }

        // Ajout des participants initiaux
        List<Commission_techniques> participants = userRepository.findAllById(dto.getParticipantIds())
                .stream()
                .filter(u -> u instanceof Commission_techniques)
                .map(u -> (Commission_techniques) u)
                .toList();

        reunion.getParticipants().addAll(participants);

        reunion = reunionRepository.save(reunion);

        // Notification aux validateurs sélectionnés
        notifierDirectionsTechniquesSelectionnees(reunion);

        return reunion;
    }

    @PreAuthorize("hasRole('DIRECTION_TECHNIQUE')")
    public void traiterValidationReunion(Long reunionId, ReunionValidationDTO dto, Direction_technique validateur) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        // Ajout dynamique des validateurs si fourni dans le DTO
        if (dto.getValidateurIds() != null && !dto.getValidateurIds().isEmpty()) {
            List<Direction_technique> validateurs = userRepository.findAllById(dto.getValidateurIds())
                    .stream()
                    .filter(u -> u instanceof Direction_technique)
                    .map(u -> (Direction_technique) u)
                    .toList();
            reunion.getValidateurs().addAll(validateurs);
        }

        ReunionState state = stateFactory.getState(reunion.getStatus());

        // Le traitement des fichiers est géré dans l'état PendingState.valider pour
        // éviter les doublons

        if (dto.isValide()) {
            state.valider(reunion, validateur, dto);
        } else {
            state.rejeter(reunion, validateur, dto.getMotifRejet());
        }
    }

    @PreAuthorize("#createur.username == authentication.name")
    public void confirmerReunion(Long reunionId, Fte createur) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        if (!reunion.getCreateur().equals(createur)) {
            throw new AccessDeniedException("Vous n'êtes pas le créateur de cette réunion");
        }

        ReunionState state = stateFactory.getState(reunion.getStatus());
        state.confirmer(reunion);
    }

    @PreAuthorize("hasRole('DIRECTION_TECHNIQUE')")
    public void rejeterReunion(Long reunionId, Direction_technique validateur, String motif) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        ReunionState state = stateFactory.getState(reunion.getStatus());
        state.rejeter(reunion, validateur, motif);
    }

    @PreAuthorize("#createur.username == authentication.name")
    public void annulerReunion(Long reunionId, Fte createur) {
        Reunion reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));

        if (!reunion.getCreateur().equals(createur)) {
            throw new AccessDeniedException("Vous n'êtes pas le créateur de cette réunion");
        }

        ReunionState state = stateFactory.getState(reunion.getStatus());
        state.annuler(reunion);
    }

    public List<ReunionResponseDTO> getReunionsPourUser(user user) {
        List<Reunion> reunions;

        if (user instanceof Fte) {
            reunions = reunionRepository.findByCreateurId(user.getId());
        } else if (user instanceof Direction_technique) {
            reunions = reunionRepository.findByValidateurId(user.getId());
        } else if (user instanceof Commission_techniques) {
            reunions = reunionRepository.findByParticipant(user);
        } else {
            reunions = java.util.Collections.emptyList();
        }

        return reunions.stream()
                .map(this::convertToDto)
                .toList();
    }

    private ReunionResponseDTO convertToDto(Reunion reunion) {
        // Conversion entity -> DTO
        ReunionResponseDTO dto = new ReunionResponseDTO();
        dto.setId(reunion.getId());
        dto.setSujet(reunion.getSujet());
        dto.setDescription(reunion.getDescription());
        dto.setDateProposee(reunion.getDateProposee());
        dto.setStatus(reunion.getStatus());

        return dto;
    }

    private void notifierDirectionsTechniquesSelectionnees(Reunion reunion) {
        String titre = "Nouvelle réunion à valider : " + reunion.getSujet();
        String message = String.format(
                "Une nouvelle réunion a été créée par %s.\n\n" +
                        "Sujet : %s\n" +
                        "Date proposée : %s\n\n" +
                        "Veuillez examiner et valider cette réunion.",
                reunion.getCreateur().getUsername(),
                reunion.getSujet(),
                reunion.getDateProposee().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

        reunion.getValidateurs().forEach(direction -> {
            notificationService.createAndSendNotification(direction, titre, message);
        });
    }

    public Resource getFichiersByReunioneIde(Long reunionId) {
        List<ReunionDocument> reunionDocuments = reunionDocumentRepository.findByReunionId(reunionId);
        if (reunionDocuments.isEmpty()) {
            throw new RuntimeException("Aucun fichier trouvé pour cette réunion");
        }

        ReunionDocument doc = reunionDocuments.get(0); // on prend le premier fichier (ou le PV final)
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(doc.getCheminFichier()));
            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier: " + doc.getNomFichier(), e);
        }
    }

    public List<ReunionDocument> getFichiersByReunionId(Long reunionId) {
        List<ReunionDocument> reunionDocuments = reunionDocumentRepository.findByReunionId(reunionId);

        if (reunionDocuments.isEmpty()) {
            throw new RuntimeException("Aucun fichier trouvé pour cette réunion");
        }

        return reunionDocuments;
    }

    // Télécharger un fichier par son id
    public Resource downloadFichier(Long fileId) {
        ReunionDocument doc = reunionDocumentRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier non trouvé avec id " + fileId));

        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(doc.getCheminFichier()));
            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier: " + doc.getNomFichier(), e);
        }
    }

}