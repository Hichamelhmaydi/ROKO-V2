package com.example.roko.config;

import com.example.roko.entity.Admin;
import com.example.roko.enums.CompteStatus;
import com.example.roko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByEmail("admin@roko.com")) {

            Admin admin = new Admin();
            admin.setNom("Admin");
            admin.setPrenom("ROKO");
            admin.setEmail("admin@roko.com");

            admin.setPassword(passwordEncoder.encode("admin123"));

            admin.setTelephone("+212600000000");
            admin.setStatus(CompteStatus.ACTIVER);

            userRepository.save(admin);
        }
    }
}