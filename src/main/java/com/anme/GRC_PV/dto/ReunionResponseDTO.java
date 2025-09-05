package com.anme.GRC_PV.dto;

import java.time.LocalDateTime;
import java.util.List;
import com.anme.GRC_PV.Entity.ReunionStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReunionResponseDTO {
    private Long id;
    private String sujet;
    private String description;
    private LocalDateTime dateProposee;
    private ReunionStatus status;
    private UserDTO createur;
    private UserDTO validateur;
    private List<UserDTO> participants;
    private List<String> documents;
}
