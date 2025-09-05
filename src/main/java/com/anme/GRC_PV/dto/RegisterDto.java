package com.anme.GRC_PV.dto;

import lombok.Data;

@Data
public class RegisterDto {

    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String password;
    // Si tu veux permettre le choix du type d'utilisateur à l'inscription,
    // décommente la ligne suivante :
    // private String usertype;

}
