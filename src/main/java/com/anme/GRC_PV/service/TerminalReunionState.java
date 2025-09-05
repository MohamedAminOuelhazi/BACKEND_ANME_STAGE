package com.anme.GRC_PV.service;

import com.anme.GRC_PV.Entity.Reunion;
import com.anme.GRC_PV.dto.ReunionValidationDTO;
import com.anme.GRC_PV.Entity.user;

public class TerminalReunionState implements ReunionState {
    @Override
    public void valider(Reunion reunion, user validateur, ReunionValidationDTO dto) {
        throw new IllegalStateException("Aucune transition possible depuis un état terminal");
    }

    @Override
    public void rejeter(Reunion reunion, user validateur, String motif) {
        throw new IllegalStateException("Aucune transition possible depuis un état terminal");
    }

    @Override
    public void confirmer(Reunion reunion) {
        throw new IllegalStateException("Aucune transition possible depuis un état terminal");
    }

    @Override
    public void annuler(Reunion reunion) {
        throw new IllegalStateException("Aucune transition possible depuis un état terminal");
    }
}