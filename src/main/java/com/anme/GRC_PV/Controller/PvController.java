package com.anme.GRC_PV.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.anme.GRC_PV.Entity.Fte;
import com.anme.GRC_PV.Entity.PVStatus;
import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.Signature;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.dto.PvCreateDTO;
import com.anme.GRC_PV.dto.PvRejectDTO;
import com.anme.GRC_PV.security.CurrentUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.anme.GRC_PV.service.PVService;

@RestController
@RequestMapping("/api/pv")
public class PvController {

    @Autowired
    private PVService pvService;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('FTE')")
    public ResponseEntity<?> create(
            @RequestPart("meta") String meta,
            @RequestPart("file") MultipartFile pdf,
            @AuthenticationPrincipal UserDetails me) throws JsonProcessingException {
        PvCreateDTO dto = objectMapper.readValue(meta, PvCreateDTO.class);
        Fte createur = (Fte) currentUserService.getCurrentUser(me);
        var pv = pvService.creerPV(dto.getReunionId(), dto.getTitre(), dto.getContenu(), createur, pdf);
        return ResponseEntity.ok(pv);
    }

    @PostMapping(path = "/{id}/sign", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DIRECTION_TECHNIQUE','COMMISSION_TECHNIQUE')")
    public ResponseEntity<Void> sign(
            @PathVariable Long id,
            @RequestPart("signature") MultipartFile signaturePng,
            @AuthenticationPrincipal UserDetails me) {
        user signer = currentUserService.getCurrentUser(me);
        pvService.signerPV(id, signer, signaturePng);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/reject")
    @PreAuthorize("hasAnyRole('DIRECTION_TECHNIQUE','COMMISSION_TECHNIQUE')")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestBody @Validated PvRejectDTO dto,
            @AuthenticationPrincipal UserDetails me) {
        user signer = currentUserService.getCurrentUser(me);
        pvService.rejeterPV(id, signer, dto.getMotif());
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{id}/upload-version", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('FTE')")
    public ResponseEntity<?> uploadVersion(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile pdf,
            @AuthenticationPrincipal UserDetails me) {
        Fte createur = (Fte) currentUserService.getCurrentUser(me);
        var pv = pvService.nouvelleVersionPV(id, createur, pdf);
        return ResponseEntity.ok(pv);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource res = pvService.telechargerPV(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PV-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }

    @GetMapping("/{id}/download-signed")
    public ResponseEntity<Resource> downloadSigned(@PathVariable Long id) {
        Resource res = pvService.telechargerPVSigné(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=PV-" + id + "-signed.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }

    @GetMapping("/reunion/{reunionId}")
    public ResponseEntity<?> getPvByReunionId(@PathVariable Long reunionId,
            @AuthenticationPrincipal UserDetails me) {
        Pv pv = pvService.getPvByReunionId(reunionId);

        // Vérifier si le PV existe
        if (pv == null) {
            return ResponseEntity.notFound().build();
        }

        // Préparer la réponse pour le frontend
        Map<String, Object> response = new HashMap<>();
        response.put("id", pv.getId());
        response.put("title", pv.getTitre());
        response.put("published", pv.getStatus() == PVStatus.ACCEPTED);
        response.put("status", pv.getStatus().toString()); // Ajout du statut

        // Vérifier les signatures
        // CORRECTION : Utiliser equalsIgnoreCase et corriger le pluriel
        boolean signedByDT = pv.getSignatures().stream()
                .anyMatch(
                        s -> s.getSignataire().getUsertype().equalsIgnoreCase("DIRECTION_TECHNIQUE") && s.isAccepte());

        boolean signedByCT = pv.getSignatures().stream()
                .anyMatch(s -> s.getSignataire().getUsertype().equalsIgnoreCase("COMMISSION_TECHNIQUES")
                        && s.isAccepte());

        response.put("signedByDT", signedByDT);
        response.put("signedByCT", signedByCT);

        System.out.println("---------------------------------------");
        System.out.println("Signatures du PV ID: " + pv.getId());
        System.out.println("---------------------------------------");

        // Parcourir toutes les signatures pour afficher les détails
        for (Signature signature : pv.getSignatures()) {
            System.out.println("Signature ID: " + signature.getId());
            System.out.println("Acceptée: " + signature.isAccepte());
            System.out.println("Date: " + signature.getDateSignature());
            System.out.println("Commentaire: " + signature.getCommentaire());

            // Afficher les informations du signataire
            if (signature.getSignataire() != null) {
                user signataire = signature.getSignataire();
                System.out.println("Signataire:");
                System.out.println("  ID: " + signataire.getId());
                System.out.println("  Nom: " + signataire.getFirstname() + " " + signataire.getLastname());
                System.out.println("  Username: " + signataire.getUsername());
                System.out.println("  Type: " + signataire.getUsertype());
            } else {
                System.out.println("Signataire: NULL");
            }
            System.out.println("---");
        }

        System.out.println("Résultat final - DT signé: " + signedByDT + ", CT signé: " + signedByCT);
        System.out.println("---------------------------------------");

        // Déterminer si l'utilisateur actuel a déjà signé
        // Vous devez récupérer l'utilisateur connecté ici
        user currentUser = currentUserService.getCurrentUser(me); // Méthode à implémenter
        boolean alreadySignedByMe = pv.getSignatures().stream()
                .anyMatch(s -> s.getSignataire().equals(currentUser));

        response.put("alreadySignedByMe", alreadySignedByMe);
        response.put("viewUrl", "/api/pv/" + pv.getId() + "/view");
        response.put("downloadUrl", "/api/pv/" + pv.getId() + "/download");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/view")
    public ResponseEntity<Resource> viewPdf(@PathVariable Long id) {
        Resource res = pvService.getLatestVersionFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }

    /*
     * @GetMapping("/{id}/download")
     * public ResponseEntity<Resource> downloadPv(@PathVariable Long id) {
     * Resource file = pvService.getLatestVersionFile(id);
     * 
     * return ResponseEntity.ok()
     * .contentType(MediaType.APPLICATION_PDF)
     * .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" +
     * file.getFilename() + "\"")
     * .body(file);
     * }
     */
}
