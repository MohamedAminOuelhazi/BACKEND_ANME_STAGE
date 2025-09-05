package com.anme.GRC_PV.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PvCreateDTO {
    @NotNull
    private Long reunionId;
    @NotBlank
    private String titre;
    @NotBlank
    private String contenu;

    public Long getReunionId() {
        return reunionId;
    }

    public void setReunionId(Long reunionId) {
        this.reunionId = reunionId;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}
