package com.example.hosptial.auth;

import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        // create roles if missing
        for (RoleName roleName : RoleName.values()) {
            roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
        }

        // create admin user
        userRepository.findByUsername("admin").orElseGet(() -> {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN).get();
            AuthUser admin = new AuthUser("admin", passwordEncoder.encode("admin123"), Set.of(adminRole));
            return userRepository.save(admin);
        });

        // create doctor user
        userRepository.findByUsername("doctor").orElseGet(() -> {
            Role doctorRole = roleRepository.findByName(RoleName.DOCTOR).get();
            AuthUser doctor = new AuthUser("doctor", passwordEncoder.encode("doctor123"), Set.of(doctorRole));
            return userRepository.save(doctor);
        });

        // create patient user
        userRepository.findByUsername("patient").orElseGet(() -> {
            Role patientRole = roleRepository.findByName(RoleName.PATIENT).get();
            AuthUser patient = new AuthUser("patient", passwordEncoder.encode("patient123"), Set.of(patientRole));
            return userRepository.save(patient);
        });
    }
}

