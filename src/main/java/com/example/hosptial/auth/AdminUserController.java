package com.example.hosptial.auth;

import java.util.Map;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import com.example.hosptial.Doctor;
import com.example.hosptial.DoctorRepository;
import com.example.hosptial.Patient;
import com.example.hosptial.PatientRepository;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    public AdminUserController(UserRepository users, RoleRepository roles, PasswordEncoder passwords, DoctorRepository doctors, PatientRepository patients) {
        this.users = users; this.roles = roles; this.passwords = passwords; this.doctors = doctors; this.patients = patients;
    }
    @GetMapping
    public java.util.List<Map<String,Object>> list() {
        return users.findAll().stream().map(u -> Map.<String,Object>of("id",u.getId(),"username",u.getUsername(),"roles",u.getRoles().stream().map(r -> r.getName().name()).toList())).toList();
    }
    @GetMapping("/unlinked-profiles")
    public Map<String,Object> unlinkedProfiles() {
        var doctorProfiles = doctors.findAll().stream().filter(d -> d.isActive() && blank(d.getAccountUsername()))
                .map(d -> Map.<String,Object>of("id", d.getId(), "name", d.getName(), "detail", d.getSpecialization())).toList();
        var patientProfiles = patients.findByArchivedFalse(org.springframework.data.domain.PageRequest.of(0, 1000)).getContent().stream()
                .filter(p -> blank(p.getAccountUsername())).map(p -> Map.<String,Object>of("id", p.getId(), "name", p.getName(), "detail", "Patient profile")).toList();
        return Map.of("doctors", doctorProfiles, "patients", patientProfiles);
    }
    @PostMapping
    @Transactional
    public Map<String,Object> create(@RequestBody CreateUserRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() || request.getPassword() == null || request.getPassword().length() < 8)
            throw new IllegalArgumentException("Username is required and password must contain at least 8 characters");
        if (request.getRole() == null || request.getRole() == RoleName.ADMIN)
            throw new IllegalArgumentException("Choose DOCTOR or PATIENT; administrator accounts must be provisioned separately");
        if (users.findByUsername(request.getUsername()).isPresent()) throw new IllegalArgumentException("Username already exists");
        Role role = roles.findByName(request.getRole()).orElseThrow();
        AuthUser saved = users.save(new AuthUser(request.getUsername(), passwords.encode(request.getPassword()), Set.of(role)));
        Long profileId;
        if (request.getRole() == RoleName.DOCTOR) {
            if (request.getPatientId() != null) throw new IllegalArgumentException("A doctor account cannot be linked to a patient record");
            if (request.getDoctorId() != null) {
                Doctor doctor = doctors.findById(request.getDoctorId()).orElseThrow(() -> new IllegalArgumentException("Doctor profile not found"));
                if (!doctor.isActive() || !blank(doctor.getAccountUsername())) throw new IllegalArgumentException("Choose an active doctor profile that has no linked login");
                doctor.setAccountUsername(saved.getUsername()); profileId = doctors.save(doctor).getId();
            } else {
                if (blank(request.getName()) || blank(request.getSpecialization())) throw new IllegalArgumentException("Doctor name and specialization are required");
                Doctor doctor = new Doctor(); doctor.setName(request.getName().trim()); doctor.setSpecialization(request.getSpecialization().trim());
                doctor.setContact(request.getContact()); doctor.setAccountUsername(saved.getUsername()); profileId = doctors.save(doctor).getId();
            }
        } else {
            if (request.getDoctorId() != null) throw new IllegalArgumentException("A patient account cannot be linked to a doctor record");
            if (request.getPatientId() != null) {
                Patient patient = patients.findById(request.getPatientId()).orElseThrow(() -> new IllegalArgumentException("Patient profile not found"));
                if (patient.isArchived() || !blank(patient.getAccountUsername())) throw new IllegalArgumentException("Choose an active patient profile that has no linked login");
                patient.setAccountUsername(saved.getUsername()); profileId = patients.save(patient).getId();
            } else {
                if (blank(request.getName()) || request.getAge() == null || request.getAge() < 0 || blank(request.getGender())) throw new IllegalArgumentException("Patient name, valid age, and gender are required");
                Patient patient = new Patient(); patient.setName(request.getName().trim()); patient.setAge(request.getAge()); patient.setGender(request.getGender().trim());
                patient.setContact(request.getContact()); patient.setAddress(request.getAddress()); patient.setAccountUsername(saved.getUsername()); profileId = patients.save(patient).getId();
            }
        }
        return Map.of("id", saved.getId(), "profileId", profileId, "username", saved.getUsername(), "roles", java.util.List.of(request.getRole().name()));
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails current) {
        AuthUser target = users.findById(id).orElseThrow();
        if (target.getUsername().equals(current.getUsername()) || target.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ADMIN))
            throw new IllegalArgumentException("Administrator accounts cannot be deleted here");
        if (doctors.findByAccountUsername(target.getUsername()).isPresent() || patients.findByAccountUsername(target.getUsername()).isPresent())
            throw new IllegalArgumentException("This account is linked to a medical record. Deactivate or archive the profile instead.");
        users.delete(target);
    }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    public static class CreateUserRequest {
        private String username; private String password; private RoleName role; private String name; private String specialization; private String contact; private String address; private String gender; private Integer age; private Long doctorId; private Long patientId;
        public String getUsername(){return username;} public void setUsername(String v){username=v;}
        public String getPassword(){return password;} public void setPassword(String v){password=v;}
        public RoleName getRole(){return role;} public void setRole(RoleName v){role=v;}
        public String getName(){return name;} public void setName(String v){name=v;}
        public String getSpecialization(){return specialization;} public void setSpecialization(String v){specialization=v;}
        public String getContact(){return contact;} public void setContact(String v){contact=v;}
        public String getAddress(){return address;} public void setAddress(String v){address=v;}
        public String getGender(){return gender;} public void setGender(String v){gender=v;}
        public Integer getAge(){return age;} public void setAge(Integer v){age=v;}
        public Long getDoctorId(){return doctorId;} public void setDoctorId(Long v){doctorId=v;}
        public Long getPatientId(){return patientId;} public void setPatientId(Long v){patientId=v;}
    }
}
