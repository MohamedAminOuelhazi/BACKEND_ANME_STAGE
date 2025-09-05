package com.anme.GRC_PV.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.anme.GRC_PV.Entity.Reunion;
import com.anme.GRC_PV.Entity.ReunionStatus;
import com.anme.GRC_PV.Entity.user;

public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByCreateurId(Long createurId);

    @Query("SELECT r FROM Reunion r WHERE :user MEMBER OF r.participants")
    List<Reunion> findByParticipant(user user);

    List<Reunion> findByStatus(ReunionStatus status);

    @Query("SELECT r FROM Reunion r JOIN r.validateurs v WHERE v.id = :validateurId")
    List<Reunion> findByValidateurId(Long validateurId);
}