package com.anme.GRC_PV.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({ "signataire", "pv" })
public class Signature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean accepte;
    private String commentaire;
    private LocalDateTime dateSignature;

    @ManyToOne(fetch = FetchType.LAZY)
    private user signataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pv_id")
    private Pv pv;

    private String cheminSignature;
}