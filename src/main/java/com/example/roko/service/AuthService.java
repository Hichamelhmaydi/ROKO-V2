package com.example.roko.service;

import com.example.roko.dto.AuthResponseDTO;
import com.example.roko.dto.LoginRequestDTO;
import com.example.roko.dto.RegisterRequestDTO;
import com.example.roko.entity.Admin;
import com.example.roko.entity.User;
import com.example.roko.entity.Voyageurs;
import com.example.roko.enums.CompteStatus;
import com.example.roko.repository.UserRepository;
import com.example.roko.security.UserPrincipal;
import com.example.roko.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    // LOGIN
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Générer le token JWT
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userPrincipal);

        // Retourner la réponse
        return new AuthResponseDTO(
                token,
                "Bearer",
                userPrincipal.getId(),
                userPrincipal.getNom(),
                userPrincipal.getPrenom(),
                userPrincipal.getEmail(),
                userPrincipal.getRole().replace("ROLE_", ""),
                "Connexion réussie"
        );
    }

    // REGISTER VOYAGEUR
    public AuthResponseDTO registerVoyageur(RegisterRequestDTO registerRequest) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        // Créer un nouveau voyageur
        Voyageurs voyageur = new Voyageurs();
        voyageur.setNom(registerRequest.getNom());
        voyageur.setPrenom(registerRequest.getPrenom());
        voyageur.setEmail(registerRequest.getEmail());
        voyageur.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        voyageur.setTelephone(registerRequest.getTelephone());
        voyageur.setIdNational(registerRequest.getIdNational());
        voyageur.setDateExpiration(registerRequest.getDateExpiration());
        voyageur.setStatus(CompteStatus.ACTIVER);

        User savedUser = userRepository.save(voyageur);

        // Générer le token JWT
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String token = jwtUtil.generateToken(userPrincipal);

        return new AuthResponseDTO(
                token,
                "Bearer",
                savedUser.getId(),
                savedUser.getNom(),
                savedUser.getPrenom(),
                savedUser.getEmail(),
                "VOYAGEUR",
                "Inscription réussie"
        );
    }

    // REGISTER ADMIN (protégé, seulement accessible par un admin)
    public AuthResponseDTO registerAdmin(RegisterRequestDTO registerRequest) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Un compte avec cet email existe déjà");
        }

        // Créer un nouveau admin
        Admin admin = new Admin();
        admin.setNom(registerRequest.getNom());
        admin.setPrenom(registerRequest.getPrenom());
        admin.setEmail(registerRequest.getEmail());
        admin.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        admin.setTelephone(registerRequest.getTelephone());
        admin.setStatus(CompteStatus.ACTIVER);

        User savedUser = userRepository.save(admin);

        // Générer le token JWT
        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String token = jwtUtil.generateToken(userPrincipal);

        return new AuthResponseDTO(
                token,
                "Bearer",
                savedUser.getId(),
                savedUser.getNom(),
                savedUser.getPrenom(),
                savedUser.getEmail(),
                "ADMIN",
                "Admin créé avec succès"
        );
    }

    // GET CURRENT USER
    @Transactional(readOnly = true)
    public UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new RuntimeException("Utilisateur non authentifié");
    }
}