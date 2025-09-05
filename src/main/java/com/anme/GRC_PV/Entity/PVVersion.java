package com.anme.GRC_PV.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PVVersion {
    @Id
    @GeneratedValue
    private UUID id;

    private LocalDateTime dateCreation;
    private String cheminFichier;
    private int version;

    @ManyToOne
    @JsonIgnore
    private Pv pv;
}