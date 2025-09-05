package com.anme.GRC_PV.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anme.GRC_PV.Entity.Direction_technique;
import com.anme.GRC_PV.Entity.Fte;
import com.anme.GRC_PV.Entity.Reunion;
import com.anme.GRC_PV.Entity.ReunionDocument;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.ReunionDocumentRepository;
import com.anme.GRC_PV.dto.ReunionRequestDTO;
import com.anme.GRC_PV.dto.ReunionResponseDTO;
import com.anme.GRC_PV.dto.ReunionValidationDTO;
import com.anme.GRC_PV.security.CurrentUserService;
import com.anme.GRC_PV.service.ReunionService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/reunions")
@CrossOrigin(origins = "http://localhost:4200")
public class ReunionController {

    @Autowired
    private ReunionService reunionService;
    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private ReunionDocumentRepository reunionDocumentRepository;

    @PostMapping("/creerReunion")
    @PreAuthorize("hasRole('FTE')")
    public ResponseEntity<ReunionResponseDTO> creerReunion(
            @RequestBody @Validated ReunionRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Fte createur = (Fte) currentUserService.getCurrentUser(userDetails);
        Reunion reunion = reunionService.creerReunion(dto, createur);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.convertToDto(reunion));
    }

    @PostMapping("/{id}/validation")
    @PreAuthorize("hasRole('DIRECTION_TECHNIQUE')")
    public ResponseEntity<Void> validerReunion(
            @PathVariable Long id,
            @ModelAttribute @Validated ReunionValidationDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Direction_technique validateur = (Direction_technique) currentUserService.getCurrentUser(userDetails);
        reunionService.traiterValidationReunion(id, dto, validateur);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/confirmation")
    @PreAuthorize("hasRole('FTE')")
    public ResponseEntity<Void> confirmerReunion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Fte createur = (Fte) currentUserService.getCurrentUser(userDetails);
        reunionService.confirmerReunion(id, createur);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rejet")
    @PreAuthorize("hasRole('DIRECTION_TECHNIQUE')")
    public ResponseEntity<Void> rejeterReunion(
            @PathVariable Long id,
            @RequestBody @Validated com.anme.GRC_PV.dto.ReunionRejetDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Direction_technique validateur = (Direction_technique) currentUserService.getCurrentUser(userDetails);
        reunionService.rejeterReunion(id, validateur, dto.getMotifRejet());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/annulation")
    @PreAuthorize("hasRole('FTE')")
    public ResponseEntity<Void> annulerReunion(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Fte createur = (Fte) currentUserService.getCurrentUser(userDetails);
        reunionService.annulerReunion(id, createur);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mesReunions")
    public ResponseEntity<List<ReunionResponseDTO>> getMesReunions(
            @AuthenticationPrincipal UserDetails userDetails) {

        user user = currentUserService.getCurrentUser(userDetails);
        List<ReunionResponseDTO> reunions = reunionService.getReunionsPourUser(user);
        return ResponseEntity.ok(reunions);
    }

    /*
     * @GetMapping("/{id}/getfichiers")
     * public ResponseEntity<Resource> getFichiersByReunion(@PathVariable Long id) {
     * Resource res = reunionService.getFichiersByReunionId(id);
     * return ResponseEntity.ok()
     * .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PV-" + id +
     * ".pdf")
     * .contentType(MediaType.APPLICATION_PDF)
     * .body(res);
     * }
     */

    // Récupérer tous les fichiers d'une réunion
    @GetMapping("/{id}/getfichiers")
    public ResponseEntity<List<ReunionDocument>> getFichiersByReunion(@PathVariable Long id) {
        List<ReunionDocument> fichiers = reunionService.getFichiersByReunionId(id);
        return ResponseEntity.ok(fichiers);
    }

    @GetMapping("/fichiers/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        Resource res = reunionService.downloadFichier(fileId);
        ReunionDocument doc = reunionDocumentRepository.findById(fileId).orElseThrow();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + doc.getNomFichier())
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }

    private ReunionResponseDTO convertToDto(Reunion reunion) {
        // Conversion entity -> DTO
        ReunionResponseDTO dto = new ReunionResponseDTO();
        dto.setId(reunion.getId());
        dto.setSujet(reunion.getSujet());
        dto.setDescription(reunion.getDescription());
        dto.setDateProposee(reunion.getDateProposee());
        dto.setStatus(reunion.getStatus());

        return dto;
    }
}