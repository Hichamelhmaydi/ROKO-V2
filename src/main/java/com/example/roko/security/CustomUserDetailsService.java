package com.example.roko.security;

import com.example.roko.entity.User;
import com.example.roko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("=== CustomUserDetailsService.loadUserByUsername ===");
        log.info("Email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Utilisateur non trouvé avec l'email: {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouvé avec l'email: " + email);
                });

        log.info("Utilisateur trouvé: {}", user.getEmail());
        log.info("Type: {}", user.getClass().getSimpleName());
        log.info("Hash password DB: {}", user.getPassword());

        UserPrincipal principal = UserPrincipal.create(user);
        log.info("UserPrincipal créé avec le rôle: {}", principal.getRole());
        log.info("isEnabled: {}", principal.isEnabled());
        log.info("isAccountNonLocked: {}", principal.isAccountNonLocked());

        return principal;
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + id));

        return UserPrincipal.create(user);
    }
}