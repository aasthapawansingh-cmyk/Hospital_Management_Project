package com.example.hosptial.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying          // ✅ tells Spring it's a DELETE/UPDATE
    @Transactional      // ✅ ensures transaction exists
    void deleteByUsername(String username);
}