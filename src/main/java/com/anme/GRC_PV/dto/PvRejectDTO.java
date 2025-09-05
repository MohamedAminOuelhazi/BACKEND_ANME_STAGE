package com.anme.GRC_PV.dto;

import jakarta.validation.constraints.NotBlank;

public class PvRejectDTO {
    @NotBlank
    private String motif;

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
