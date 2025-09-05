package com.anme.GRC_PV.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.anme.GRC_PV.Entity.PVStatus;
import com.anme.GRC_PV.Entity.PVVersion;
import com.anme.GRC_PV.Entity.Pv;
import com.anme.GRC_PV.Entity.Signature;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.PvRepository;
import com.anme.GRC_PV.Repository.ReunionRepository;
import com.anme.GRC_PV.Repository.SignatureRepository;
import com.anme.GRC_PV.Repository.userRepo;

@Service
@Transactional
public class PVService {

    @Autowired
    private PvRepository pvRepository;
    @Autowired
    private SignatureRepository signatureRepository;
    @Autowired
    private ReunionRepository reunionRepository;
    @Autowired
    private userRepo userRepository;
    @Autowired
    private PVStateFactory pvStateFactory;
    @Autowired
    private PVNotificationService notificationService;
    @Autowired
    private FileStorageService fileStorageService;

    public Pv creerPV(Long reunionId, String titre, String contenu, user createur, MultipartFile pdf) {
        var reunion = reunionRepository.findById(reunionId)
                .orElseThrow(() -> new RuntimeException("Réunion non trouvée"));
        if (!reunion.getCreateur().equals(createur)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à créer un PV pour cette réunion");
        }

        String cheminPdf = fileStorageService.stockerFichier(pdf);

        Pv pv = new Pv();
        pv.setTitre(titre);
        pv.setContenu(contenu);
        pv.setReunion(reunion);
        pv.setStatus(PVStatus.PENDING);

        PVVersion v = new PVVersion();
        v.setDateCreation(LocalDateTime.now());
        v.setCheminFichier(cheminPdf);
        v.setVersion(1);
        v.setPv(pv);
        pv.getVersions().add(v);

        pv = pvRepository.save(pv);
        notificationService.notifierNouveauPV(pv);
        return pv;
    }

    public Pv signerPV(Long pvId, user signataire, MultipartFile signaturePng) {
        Pv pv = pvRepository.findById(pvId).orElseThrow();

        // Idempotence: déjà accepté
        if (pv.getStatus() == PVStatus.ACCEPTED) {
            return pv;
        }

        // Vérifier l'autorisation du signataire
        if (!isSignerAutorise(pv, signataire)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à signer ce PV");
        }

        // Empêcher double signature du même user
        boolean alreadySigned = pv.getSignatures().stream()
                .anyMatch(s -> s.isAccepte() && s.getSignataire() != null
                        && s.getSignataire().getId() == signataire.getId());
        if (alreadySigned) {
            return pv;
        }

        String chemin = fileStorageService.stockerFichier(signaturePng);
        Signature s = new Signature();
        s.setAccepte(true);
        s.setCommentaire(null);
        s.setDateSignature(LocalDateTime.now());
        s.setSignataire(signataire);
        s.setPv(pv);
        s.setCheminSignature(chemin);
        pv.getSignatures().add(s);

        if (pv.getStatus() == PVStatus.PENDING && tousOntSigne(pv)) {
            var state = pvStateFactory.getState(pv.getStatus());
            state.accepter(pv, signataire);
            notificationService.notifierPVCompletementSigne(pv);
        }

        return pvRepository.save(pv);
    }

    public void rejeterPV(Long pvId, user signataire, String motif) {
        Pv pv = pvRepository.findById(pvId).orElseThrow();

        // Vérifier l'autorisation du signataire
        if (!isSignerAutorise(pv, signataire)) {
            throw new RuntimeException("Vous n'êtes pas autorisé à rejeter ce PV");
        }

        var state = pvStateFactory.getState(pv.getStatus());
        state.rejeter(pv, signataire, motif);

        Signature s = new Signature();
        s.setAccepte(false);
        s.setCommentaire(motif);
        s.setDateSignature(LocalDateTime.now());
        s.setSignataire(signataire);
        s.setPv(pv);
        pv.getSignatures().add(s);

        pvRepository.save(pv);
    }

    public Pv nouvelleVersionPV(Long pvId, user createur, MultipartFile pdf) {
        Pv pv = pvRepository.findById(pvId).orElseThrow();
        if (!pv.getReunion().getCreateur().equals(createur)) {
            throw new RuntimeException("Seul le créateur peut téléverser une nouvelle version du PV");
        }

        String cheminPdf = fileStorageService.stockerFichier(pdf);

        int next = pv.getVersions().stream().mapToInt(PVVersion::getVersion).max().orElse(0) + 1;
        PVVersion v = new PVVersion();
        v.setDateCreation(LocalDateTime.now());
        v.setCheminFichier(cheminPdf);
        v.setVersion(next);
        v.setPv(pv);
        pv.getVersions().add(v);
        pv.getSignatures().clear();
        pv.setStatus(PVStatus.PENDING);

        notificationService.notifierNouvelleVersionPV(pv);
        return pvRepository.save(pv);
    }

    public Resource telechargerPV(Long pvId) {
        Pv pv = pvRepository.findById(pvId).orElseThrow();
        PVVersion latest = pv.getVersions().stream().max(Comparator.comparingInt(PVVersion::getVersion)).orElseThrow();
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(latest.getCheminFichier()));
            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier", e);
        }
    }

    public Resource telechargerPVSigné(Long pvId) {
        Pv pv = pvRepository.findById(pvId).orElseThrow();
        if (pv.getStatus() != PVStatus.ACCEPTED) {
            throw new RuntimeException("Le PV n'est pas encore signé par tous");
        }
        PVVersion latest = pv.getVersions().stream().max(Comparator.comparingInt(PVVersion::getVersion)).orElseThrow();
        try {
            byte[] base = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(latest.getCheminFichier()));
            try (PDDocument doc = PDDocument.load(new ByteArrayInputStream(base))) {
                var page = doc.getPage(0);
                try (PDPageContentStream content = new PDPageContentStream(doc, page,
                        PDPageContentStream.AppendMode.APPEND, true, true)) {
                    float y = 100;
                    for (Signature s : pv.getSignatures()) {
                        if (s.getCheminSignature() == null)
                            continue;
                        byte[] img = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(s.getCheminSignature()));
                        PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, img, "sig");
                        content.drawImage(pdImage, 50, y, 150, 50);
                        y += 60;
                    }
                }
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                doc.save(bos);
                return new ByteArrayResource(bos.toByteArray());
            }
        } catch (IOException e) {
            throw new RuntimeException("Erreur génération PDF signé", e);
        }
    }

    public List<Pv> getPVsPourUser(user user) {
        return pvRepository.findAll();
    }

    private boolean tousOntSigne(Pv pv) {
        Set<Long> required = requiredSignerIds(pv);
        Set<Long> accepted = pv.getSignatures().stream()
                .filter(Signature::isAccepte)
                .filter(s -> s.getSignataire() != null)
                .map(s -> s.getSignataire().getId())
                .collect(Collectors.toSet());
        return !required.isEmpty() && accepted.containsAll(required);
    }

    private boolean isSignerAutorise(Pv pv, user signataire) {
        return requiredSignerIds(pv).contains(signataire.getId());
    }

    private Set<Long> requiredSignerIds(Pv pv) {
        Set<Long> ids = new HashSet<>();
        pv.getReunion().getValidateurs().forEach(d -> ids.add(d.getId()));
        pv.getReunion().getParticipants().forEach(c -> ids.add(c.getId()));
        return ids;
    }

    public Pv getPvByReunionId(Long reunionId) {
        return pvRepository.findByReunionId(reunionId)
                .orElseThrow(() -> new RuntimeException("Aucun PV trouvé pour cette réunion"));
    }

    // Récupérer PV par id
    public Pv getPvById(Long id) {
        return pvRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("PV introuvable avec l'id : " + id));
    }

    public Resource getLatestVersionFile(Long pvId) {
        Pv pv = pvRepository.findById(pvId)
                .orElseThrow(() -> new RuntimeException("PV introuvable avec l'id : " + pvId));

        PVVersion latest = pv.getVersions().stream()
                .max(Comparator.comparingInt(PVVersion::getVersion))
                .orElseThrow(() -> new RuntimeException("Aucune version disponible pour le PV " + pvId));

        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Path.of(latest.getCheminFichier()));
            return new ByteArrayResource(bytes);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lecture fichier", e);
        }
    }
}