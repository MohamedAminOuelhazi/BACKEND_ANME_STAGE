package com.anme.GRC_PV.service;

import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RejectedPVState implements PVState {

    @Autowired
    private PVNotificationService notificationService;

    @Override
    public void accepter(Pv pv, user signataire) {
        throw new IllegalStateException("Impossible d'accepter un PV rejeté");
    }

    @Override
    public void rejeter(Pv pv, user signataire, String motif) {
        throw new IllegalStateException("PV déjà rejeté");
    }

    @Override
    public void nouvelleVersion(Pv pv, user createur) {
        // Autoriser la création d'une nouvelle version après rejet
        // Notification WebSocket de nouvelle version
        notificationService.notifierNouvelleVersionPV(pv);
    }
}