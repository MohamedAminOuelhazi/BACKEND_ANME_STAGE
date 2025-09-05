package com.anme.GRC_PV.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anme.GRC_PV.Entity.Pv;

public interface PvRepository extends JpaRepository<Pv, Long> {
    Optional<Pv> findByReunionId(Long reunionId);

    Optional<Pv> findById(Long id);

}
