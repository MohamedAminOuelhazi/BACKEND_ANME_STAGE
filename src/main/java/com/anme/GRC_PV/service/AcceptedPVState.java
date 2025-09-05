package com.anme.GRC_PV.service;

import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.user;
import org.springframework.stereotype.Service;

@Service
public class AcceptedPVState implements PVState {
    @Override
    public void accepter(Pv pv, user signataire) {
        throw new IllegalStateException("PV déjà accepté");
    }

    @Override
    public void rejeter(Pv pv, user signataire, String motif) {
        throw new IllegalStateException("Impossible de rejeter un PV accepté");
    }

    @Override
    public void nouvelleVersion(Pv pv, user createur) {
        throw new IllegalStateException("Impossible d'ajouter une nouvelle version à un PV accepté");
    }
}