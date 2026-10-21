package com.example.hosptial;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long>{
    java.util.Optional<Doctor> findByAccountUsername(String username);
    
}
