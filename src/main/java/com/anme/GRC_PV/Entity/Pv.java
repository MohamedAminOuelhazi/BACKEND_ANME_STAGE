package com.anme.GRC_PV.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties({ "reunion" })
public class Pv {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private String titre;

    @Lob
    private String contenu;

    @Enumerated(EnumType.STRING)
    private PVStatus status = PVStatus.PENDING;

    public PVStatus getStatus() {
        return status;
    }

    public void setStatus(PVStatus status) {
        this.status = status;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Reunion getReunion() {
        return reunion;
    }

    public void setReunion(Reunion reunion) {
        this.reunion = reunion;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    @OneToMany(mappedBy = "pv", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("version DESC")
    private List<PVVersion> versions = new ArrayList<>();

    @OneToMany(mappedBy = "pv", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Signature> signatures = new ArrayList<>();

    @ManyToOne
    private Reunion reunion;

    // Getters/Setters complémentaires
    public List<PVVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<PVVersion> versions) {
        this.versions = versions;
    }

    public List<Signature> getSignatures() {
        return signatures;
    }

    public void setSignatures(List<Signature> signatures) {
        this.signatures = signatures;
    }
}