package com.anme.GRC_PV.Entity;

public enum ReunionStatus {
    PENDING, // Créée par FTE, en attente validation direction
    SCHEDULED, // Validée par direction
    VALIDATED, // Confirmée par FTE
    REJECTED, // Rejetée par direction
    CANCELLED // Annulée par FTE
}