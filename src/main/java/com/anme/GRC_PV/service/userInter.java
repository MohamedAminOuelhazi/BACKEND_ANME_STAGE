package com.anme.GRC_PV.service;

import java.util.List;

import com.anme.GRC_PV.Entity.user;

public interface userInter {

    public boolean checkUsernameEmailAvailability(String username, String email);

    user registerUser(com.anme.GRC_PV.dto.RegisterDto registerDto, String password, String usertype);

    public List<user> getUsersByRole(String role);

}
