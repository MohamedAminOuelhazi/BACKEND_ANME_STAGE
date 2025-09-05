package com.anme.GRC_PV.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReunionValidationDTO {

    private boolean valide;
    private String motifRejet;
    private List<MultipartFile> fichiers;
    private List<Long> participantIds;
    private List<Long> validateurIds;

}
