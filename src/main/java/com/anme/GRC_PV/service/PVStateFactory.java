package com.anme.GRC_PV.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import com.anme.GRC_PV.Entity.PVStatus;

@Service
public class PVStateFactory {
    @Autowired
    private ApplicationContext context;

    public PVState getState(PVStatus status) {
        return switch (status) {
            case PENDING -> context.getBean(PendingPVState.class);
            case ACCEPTED -> context.getBean(AcceptedPVState.class);
            case REJECTED -> context.getBean(RejectedPVState.class);
            default -> throw new IllegalArgumentException("Etat non supporté : " + status);
        };
    }
}