package com.anme.GRC_PV.dto;

import jakarta.validation.constraints.NotBlank;

public class ReunionRejetDTO {
    @NotBlank
    private String motifRejet;

    public String getMotifRejet() {
        return motifRejet;
    }

    public void setMotifRejet(String motifRejet) {
        this.motifRejet = motifRejet;
    }
}
