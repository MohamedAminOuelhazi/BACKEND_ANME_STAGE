package com.anme.GRC_PV.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.anme.GRC_PV.Entity.Reunion;
import com.anme.GRC_PV.Entity.ReunionStatus;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.ReunionRepository;
import com.anme.GRC_PV.dto.ReunionValidationDTO;

@Service
@Scope("prototype")
public class ScheduledState implements ReunionState {

    @Autowired
    private ReunionRepository reunionRepository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public void confirmer(Reunion reunion) {
        reunion.setStatus(ReunionStatus.VALIDATED);
        reunionRepository.save(reunion);

        // Notification WebSocket de confirmation finale
        notificationService.notifierConfirmationFinale(reunion);
    }

    @Override
    public void annuler(Reunion reunion) {
        reunion.setStatus(ReunionStatus.CANCELLED);
        reunionRepository.save(reunion);

        // Notification WebSocket d'annulation
        notificationService.notifierAnnulation(reunion);
    }

    @Override
    public void valider(Reunion reunion, user validateur, ReunionValidationDTO dto) {
        throw new IllegalStateException("Impossible de valider une réunion déjà programmée");
    }

    @Override
    public void rejeter(Reunion reunion, user validateur, String motif) {
        throw new IllegalStateException("Impossible de rejeter une réunion déjà programmée");
    }
}