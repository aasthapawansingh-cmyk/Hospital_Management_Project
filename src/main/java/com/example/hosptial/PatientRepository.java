package com.example.hosptial;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient,Long> {
    java.util.Optional<Patient> findByAccountUsername(String username);
    
}
