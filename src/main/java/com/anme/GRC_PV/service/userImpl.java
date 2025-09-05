package com.anme.GRC_PV.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.anme.GRC_PV.dto.RegisterDto;
import com.anme.GRC_PV.Entity.Commission_techniques;
import com.anme.GRC_PV.Entity.Direction_technique;
import com.anme.GRC_PV.Entity.Fte;
import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.userRepo;

@Service
public class userImpl implements userInter {

  @Autowired
  userRepo userRepo;

  @Autowired
  PasswordEncoder passwordEncoder;

  @Override
  public boolean checkUsernameEmailAvailability(String username, String email) {
    // Vérifiez si le nom d'utilisateur ou l'e-mail existent déjà dans la base de
    // données
    boolean usernameExists = userRepo.existsByUsername(username);
    boolean emailExists = userRepo.existsByEmail(email);

    // Retournez false si le nom d'utilisateur ou l'e-mail existent déjà
    return !(usernameExists || emailExists);
  }

  @Override
  public user registerUser(RegisterDto registerDto, String password, String usertype) {
    user newUser;
    if ("Fte".equalsIgnoreCase(usertype)) {
      newUser = new Fte();
    } else if ("Direction_technique".equalsIgnoreCase(usertype)) {
      newUser = new Direction_technique();
    } else if ("Commission_techniques".equalsIgnoreCase(usertype)) {
      newUser = new Commission_techniques();
    } else {
      // Par défaut, créer un utilisateur de base
      newUser = new user();
    }
    newUser.setFirstname(registerDto.getFirstname());
    newUser.setLastname(registerDto.getLastname());
    newUser.setUsername(registerDto.getUsername());
    newUser.setEmail(registerDto.getEmail());
    newUser.setPassword(passwordEncoder.encode(password));
    newUser.setUsertype(usertype);
    return userRepo.save(newUser);
  }

  @Override
  public List<user> getUsersByRole(String usertype) {
    return userRepo.findByUsertype(usertype);
  }

}
