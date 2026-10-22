package com.example.hosptial.auth;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final com.example.hosptial.DoctorRepository doctorRepository;

    public CustomUserDetailsService(UserRepository userRepository, com.example.hosptial.DoctorRepository doctorRepository) {
        this.userRepository = userRepository; this.doctorRepository = doctorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new User(
            user.getUsername(),
            user.getPassword(),
            user.isEnabled() && (!user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.DOCTOR)
                || isLinkedDoctorActive(username)),
            true, true, true,
            user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList())
        );
    }

    private boolean isLinkedDoctorActive(String username) {
        return doctorRepository.findByAccountUsername(username).map(com.example.hosptial.Doctor::isActive).orElse(true);
    }

}

