package com.example.roko.controller;

import com.example.roko.dto.response.AuthResponseDTO;
import com.example.roko.dto.request.LoginRequestDTO;
import com.example.roko.dto.response.MessageResponseDTO;
import com.example.roko.dto.request.RegisterRequestDTO;
import com.example.roko.security.UserPrincipal;
import com.example.roko.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            AuthResponseDTO response = authService.login(loginRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Email ou mot de passe incorrect"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerVoyageur(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            AuthResponseDTO response = authService.registerVoyageur(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponseDTO(false, e.getMessage()));
        }
    }

    @PostMapping("/register-admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            AuthResponseDTO response = authService.registerAdmin(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponseDTO(false, e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            UserPrincipal currentUser = authService.getCurrentUser();
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponseDTO(false, "Non authentifié"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        return ResponseEntity.ok(new MessageResponseDTO(true, "Déconnexion réussie"));
    }
}