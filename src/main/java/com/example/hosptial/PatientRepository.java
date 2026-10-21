package com.example.hosptial;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient,Long> {
    java.util.Optional<Patient> findByAccountUsername(String username);
    java.util.Optional<Patient> findByAccountUsernameAndArchivedFalse(String username);
    org.springframework.data.domain.Page<Patient> findByArchivedFalse(org.springframework.data.domain.Pageable pageable);
}
