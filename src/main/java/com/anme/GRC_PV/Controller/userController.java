package com.anme.GRC_PV.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.anme.GRC_PV.Entity.user;
import com.anme.GRC_PV.Repository.userRepo;
import com.anme.GRC_PV.dto.AuthResponseDTO;
import com.anme.GRC_PV.dto.LoginDto;
import com.anme.GRC_PV.dto.checkDTO;
import com.anme.GRC_PV.dto.RegisterDto;
import com.anme.GRC_PV.dto.RoleRequestDTO;
import com.anme.GRC_PV.security.JWTGenerator;
import com.anme.GRC_PV.service.userInter;

@RestController
@RequestMapping("api/auth")
public class userController {

    private final AuthenticationManager authenticationManager;
    private final JWTGenerator jwtGenerator;

    @Autowired
    public userController(
            AuthenticationManager authenticationManager,
            JWTGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.jwtGenerator = jwtGenerator;
    }

    @Autowired
    userRepo userRepo;

    @Autowired
    private userInter userInter;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.getUsername(),
                            loginDto.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtGenerator.generateToken(authentication);
            Optional<user> user = userRepo.findByUsername(loginDto.getUsername());
            String role = user.get().getUsertype();

            return new ResponseEntity<>(new AuthResponseDTO(token, role), HttpStatus.OK);

        } catch (AuthenticationException e) {
            // Authentication failed, return an error message
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/Fte/register")
    public ResponseEntity<?> register_fte(@RequestBody RegisterDto registerDto) {
        // Prendre le mot de passe depuis le DTO
        String password = registerDto.getPassword();
        String usertype = "Fte"; // Par défaut, ou décommente si tu ajoutes usertype dans RegisterDto
        // if (registerDto.getUsertype() != null) usertype = registerDto.getUsertype();

        // Vérifier la disponibilité du username/email
        boolean available = userInter.checkUsernameEmailAvailability(registerDto.getUsername(), registerDto.getEmail());
        if (!available) {
            return new ResponseEntity<>("Username or email already exists", HttpStatus.BAD_REQUEST);
        }
        // Créer l'utilisateur
        user newUser = userInter.registerUser(registerDto, password, usertype);
        // Retourner uniquement les infos nécessaires (sans token)
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterDto() {
            {
                setFirstname(newUser.getFirstname());
                setLastname(newUser.getLastname());
                setUsername(newUser.getUsername());
                setEmail(newUser.getEmail());
                // setUsertype(newUser.getUsertype()); // décommente si tu ajoutes usertype dans
                // RegisterDto
            }
        });
    }

    @PostMapping("/Direction_technique/register")
    public ResponseEntity<?> register_Direction_technique(@RequestBody RegisterDto registerDto) {
        // Prendre le mot de passe depuis le DTO
        String password = registerDto.getPassword();
        String usertype = "Direction_technique"; // Par défaut, ou décommente si tu ajoutes usertype dans RegisterDto
        // if (registerDto.getUsertype() != null) usertype = registerDto.getUsertype();

        // Vérifier la disponibilité du username/email
        boolean available = userInter.checkUsernameEmailAvailability(registerDto.getUsername(), registerDto.getEmail());
        if (!available) {
            return new ResponseEntity<>("Username or email already exists", HttpStatus.BAD_REQUEST);
        }
        // Créer l'utilisateur
        user newUser = userInter.registerUser(registerDto, password, usertype);
        // Retourner uniquement les infos nécessaires (sans token)
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterDto() {
            {
                setFirstname(newUser.getFirstname());
                setLastname(newUser.getLastname());
                setUsername(newUser.getUsername());
                setEmail(newUser.getEmail());
                // setUsertype(newUser.getUsertype()); // décommente si tu ajoutes usertype dans
                // RegisterDto
            }
        });
    }

    @PostMapping("/Commission_techniques/register")
    public ResponseEntity<?> register_Commission_techniques(@RequestBody RegisterDto registerDto) {
        // Prendre le mot de passe depuis le DTO
        String password = registerDto.getPassword();
        String usertype = "Commission_techniques"; // Par défaut, ou décommente si tu ajoutes usertype dans RegisterDto
        // if (registerDto.getUsertype() != null) usertype = registerDto.getUsertype();

        // Vérifier la disponibilité du username/email
        boolean available = userInter.checkUsernameEmailAvailability(registerDto.getUsername(), registerDto.getEmail());
        if (!available) {
            return new ResponseEntity<>("Username or email already exists", HttpStatus.BAD_REQUEST);
        }
        // Créer l'utilisateur
        user newUser = userInter.registerUser(registerDto, password, usertype);
        // Retourner uniquement les infos nécessaires (sans token)
        return ResponseEntity.status(HttpStatus.CREATED).body(new RegisterDto() {
            {
                setFirstname(newUser.getFirstname());
                setLastname(newUser.getLastname());
                setUsername(newUser.getUsername());
                setEmail(newUser.getEmail());
                // setUsertype(newUser.getUsertype()); // décommente si tu ajoutes usertype dans
                // RegisterDto
            }
        });
    }

    @PostMapping("/checkUsernameEmailAvailability")
    public ResponseEntity<?> checkUsernameEmailAvailability(@RequestBody checkDTO user) {
        boolean isAvailable = userInter.checkUsernameEmailAvailability(user.getUsername(), user.getEmail());
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/byRole")
    public ResponseEntity<List<user>> getUsersByRole(@RequestParam String role) {
        List<user> users = userInter.getUsersByRole(role);
        if (users.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(users);
    }
}
