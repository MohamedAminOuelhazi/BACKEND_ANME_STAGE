package com.anme.GRC_PV.service;

import com.anme.GRC_PV.Entity.Reunion;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.dto.ReunionValidationDTO;

public interface ReunionState {
    void valider(Reunion reunion, user validateur, ReunionValidationDTO dto);

    void rejeter(Reunion reunion, user validateur, String motif);

    void confirmer(Reunion reunion);

    void annuler(Reunion reunion);
}