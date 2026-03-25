package com.example.roko.config;

import com.example.roko.entity.Admin;
import com.example.roko.enums.CompteStatus;
import com.example.roko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("docker")
@RequiredArgsConstructor
@Slf4j
public class DockerAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.enabled:true}")
    private boolean enabled;

    @Value("${app.default-admin.email:admin@roko.com}")
    private String email;

    @Value("${app.default-admin.password:admin}")
    private String password;

    @Value("${app.default-admin.nom:Admin}")
    private String nom;

    @Value("${app.default-admin.prenom:Roko}")
    private String prenom;

    @Value("${app.default-admin.telephone:0600000000}")
    private String telephone;

    @Override
    public void run(String... args) {
        if (!enabled) {
            log.info("Default docker admin seeding disabled");
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.info("Default admin already exists: {}", email);
            return;
        }

        Admin admin = new Admin();
        admin.setNom(nom);
        admin.setPrenom(prenom);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setTelephone(telephone);
        admin.setActif(true);
        admin.setBloque(false);
        admin.setStatus(CompteStatus.ACTIVER);

        userRepository.save(admin);
        log.info("Default docker admin created: {}", email);
    }
}
