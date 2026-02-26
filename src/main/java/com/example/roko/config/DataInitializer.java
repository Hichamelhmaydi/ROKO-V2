package com.example.roko.config;

import com.example.roko.entity.Admin;
import com.example.roko.enums.CompteStatus;
import com.example.roko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@roko.com")) {
            Admin admin = new Admin();
            admin.setNom("Admin");
            admin.setPrenom("ROKO");
            admin.setEmail("admin@roko.com");
            admin.setPassword("admin123"); 
            admin.setTelephone("+212600000000");
            admin.setStatus(CompteStatus.ACTIVER);

            userRepository.save(admin);
            System.out.println(" Admin par défaut créé: admin@roko.com / admin123");
        } else {
            System.out.println("ℹ Admin déjà existant dans la base de données");
        }
    }
}