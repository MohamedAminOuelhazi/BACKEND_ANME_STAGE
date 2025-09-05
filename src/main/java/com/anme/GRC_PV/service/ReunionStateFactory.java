package com.anme.GRC_PV.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.anme.GRC_PV.Entity.ReunionStatus;

// Factory pour les états
@Service
public class ReunionStateFactory {

    @Autowired
    private ApplicationContext context;

    public ReunionState getState(ReunionStatus status) {
        return switch (status) {
            case PENDING -> context.getBean(PendingState.class);
            case SCHEDULED -> context.getBean(ScheduledState.class);
            case VALIDATED, REJECTED, CANCELLED -> new TerminalReunionState();
            default -> throw new IllegalArgumentException("Etat non supporté : " + status);
        };
    }
}