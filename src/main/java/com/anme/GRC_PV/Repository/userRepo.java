package com.anme.GRC_PV.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.anme.GRC_PV.Entity.user;

@Repository
public interface userRepo extends JpaRepository<user, Long> {

    boolean existsByusername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<user> findByUsername(String username);

    List<user> findByUsertype(String usertype);

}
