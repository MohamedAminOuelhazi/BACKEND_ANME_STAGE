package com.anme.GRC_PV.Repository;

import com.anme.GRC_PV.Entity.ReunionDocument;

import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReunionDocumentRepository extends JpaRepository<ReunionDocument, Long> {
    List<ReunionDocument> findByReunionId(Long reunionId);
}
