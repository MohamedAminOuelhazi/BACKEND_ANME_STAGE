package com.anme.GRC_PV.service;

import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.user;

public interface PVState {
    void accepter(Pv pv, user signataire);

    void rejeter(Pv pv, user signataire, String motif);

    void nouvelleVersion(Pv pv, user createur);
}