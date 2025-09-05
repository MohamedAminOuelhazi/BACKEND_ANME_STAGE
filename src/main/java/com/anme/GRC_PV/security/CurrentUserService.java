package com.anme.GRC_PV.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.userRepo;

@Service
public class CurrentUserService {
    @Autowired
    private userRepo userRepo;

    public user getCurrentUser(UserDetails userDetails) {
        return userRepo.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
}