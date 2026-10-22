package com.example.hosptial.auth;

import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.hosptial.Doctor;
import com.example.hosptial.DoctorRepository;
import com.example.hosptial.Patient;
import com.example.hosptial.PatientRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public DataSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder,
                      DoctorRepository doctorRepository, PatientRepository patientRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
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

        // Keep the demo accounts useful by ensuring their server-side profiles exist and are linked.
        if (doctorRepository.findByAccountUsername("doctor").isEmpty()) {
            Doctor doctorProfile = new Doctor();
            doctorProfile.setName("Demo Doctor");
            doctorProfile.setSpecialization("General Medicine");
            doctorProfile.setAccountUsername("doctor");
            doctorRepository.save(doctorProfile);
        }
        if (patientRepository.findByAccountUsername("patient").isEmpty()) {
            Patient patientProfile = new Patient();
            patientProfile.setName("Demo Patient");
            patientProfile.setAge(30);
            patientProfile.setGender("Not specified");
            patientProfile.setAccountUsername("patient");
            patientRepository.save(patientProfile);
        }
    }
}

