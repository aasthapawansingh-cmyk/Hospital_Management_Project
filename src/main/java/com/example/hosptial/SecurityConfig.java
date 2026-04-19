package com.example.hosptial;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.hosptial.auth.CustomUserDetailsService;
import com.example.hosptial.auth.JwtAuthFilter;
import org.springframework.security.authentication.AuthenticationManager;


import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;

import com.example.hosptial.auth.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    public SecurityConfig(JwtService jwtService,CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
         this.userDetailsService = userDetailsService;
    }
      @Bean
public JwtAuthFilter jwtAuthFilter() {
    return new JwtAuthFilter(jwtService, userDetailsService);
}
    @Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}

    @Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    return config.getAuthenticationManager();
}

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
.requestMatchers("/auth/**").permitAll()

            .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("ADMIN", "RECEPTION")
            .requestMatchers(HttpMethod.POST, "/api/v1/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/**").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
            .anyRequest().permitAll()
        )
       .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}
   