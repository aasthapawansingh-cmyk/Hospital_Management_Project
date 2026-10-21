package com.example.hosptial;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long>{
    java.util.Optional<Doctor> findByAccountUsername(String username);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from Doctor d where d.id = :id")
    java.util.Optional<Doctor> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
