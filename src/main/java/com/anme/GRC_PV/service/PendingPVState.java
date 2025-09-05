package com.anme.GRC_PV.service;

import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.PVStatus;
import com.anme.GRC_PV.Entity.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PendingPVState implements PVState {

    @Autowired
    private PVNotificationService notificationService;

    @Override
    public void accepter(Pv pv, user signataire) {
        pv.setStatus(PVStatus.ACCEPTED);
        // Notification WebSocket d'acceptation
        notificationService.notifierPVAccepte(pv, signataire);
    }

    @Override
    public void rejeter(Pv pv, user signataire, String motif) {
        pv.setStatus(PVStatus.REJECTED);
        // Notification WebSocket de rejet
        notificationService.notifierPVRejete(pv, signataire, motif);
    }

    @Override
    public void nouvelleVersion(Pv pv, user createur) {
        throw new IllegalStateException("Impossible d'ajouter une nouvelle version à un PV en attente");
    }
}