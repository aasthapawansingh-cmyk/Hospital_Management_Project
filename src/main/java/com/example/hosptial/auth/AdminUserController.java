package com.example.hosptial.auth;

import java.util.Map;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;
    public AdminUserController(UserRepository users, RoleRepository roles, PasswordEncoder passwords) {
        this.users = users; this.roles = roles; this.passwords = passwords;
    }
    @GetMapping
    public java.util.List<Map<String,Object>> list() {
        return users.findAll().stream().map(u -> Map.<String,Object>of("id",u.getId(),"username",u.getUsername(),"roles",u.getRoles().stream().map(r -> r.getName().name()).toList())).toList();
    }
    @PostMapping
    public Map<String,Object> create(@RequestBody CreateUserRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() || request.getPassword() == null || request.getPassword().length() < 8)
            throw new IllegalArgumentException("Username is required and password must contain at least 8 characters");
        if (request.getRole() == null || request.getRole() == RoleName.ADMIN)
            throw new IllegalArgumentException("Choose DOCTOR or PATIENT; administrator accounts must be provisioned separately");
        if (users.findByUsername(request.getUsername()).isPresent()) throw new IllegalArgumentException("Username already exists");
        Role role = roles.findByName(request.getRole()).orElseThrow();
        AuthUser saved = users.save(new AuthUser(request.getUsername(), passwords.encode(request.getPassword()), Set.of(role)));
        return Map.of("id", saved.getId(), "username", saved.getUsername(), "roles", java.util.List.of(request.getRole().name()));
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails current) {
        AuthUser target = users.findById(id).orElseThrow();
        if (target.getUsername().equals(current.getUsername()) || target.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN))
            throw new IllegalArgumentException("Administrator accounts cannot be deleted here");
        users.delete(target);
    }
    public static class CreateUserRequest {
        private String username; private String password; private RoleName role;
        public String getUsername(){return username;} public void setUsername(String v){username=v;}
        public String getPassword(){return password;} public void setPassword(String v){password=v;}
        public RoleName getRole(){return role;} public void setRole(RoleName v){role=v;}
    }
}
