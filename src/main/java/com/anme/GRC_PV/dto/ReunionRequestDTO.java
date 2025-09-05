package com.anme.GRC_PV.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReunionRequestDTO {

    private String sujet;

    private String description;

    private LocalDateTime dateProposee;

    private List<Long> participantIds;

    private List<Long> validateurIds;

}
