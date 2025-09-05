package com.anme.GRC_PV.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.anme.GRC_PV.Repository.userRepo;

import com.anme.GRC_PV.Entity.user;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private userRepo userRepo;

    @Autowired
    public CustomUserDetailsService(userRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        user user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        return new User(user.getUsername(), user.getPassword(), mapRolesToAuthorities(user));
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(user user) {
        String userType = user.getUsertype();
        System.out.println("Mapping roles for userType: " + userType);

        Collection<GrantedAuthority> authorities;
        if ("Fte".equals(userType)) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_FTE"));
        } else if ("Commission_techniques".equals(userType)) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_COMMISSION_TECHNIQUE"));
        } else if ("Direction_technique".equals(userType)) {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_DIRECTION_TECHNIQUE"));
        } else {
            System.out.println("WARNING: Unknown user type: " + userType);
            authorities = Collections.emptyList();
        }

        System.out.println("Granted authorities: " + authorities);
        return authorities;
    }

}
