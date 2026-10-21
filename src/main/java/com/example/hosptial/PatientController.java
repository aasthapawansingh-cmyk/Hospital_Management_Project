package com.example.hosptial;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

@RestController
@RequestMapping("api/v1/patient")
public class PatientController {
    private final PatientService patientService;
    private final AppointmentRepository appointmentRepository;

    public PatientController(PatientService patientService, AppointmentRepository appointmentRepository) {
        this.patientService = patientService;
        this.appointmentRepository = appointmentRepository;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void addPatient( @Valid @RequestBody PatientReqDTO req) {
        Patient patient =PatientMapper.toEntity(req);
        patientService.addPatient(patient);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public Page<Patient> getAllPatient(Pageable pageable) {
        return patientService.getAllPatient(pageable);
    }

    @PreAuthorize("hasRole('PATIENT')")
    @GetMapping("/me")
    public Patient getMyProfile(@AuthenticationPrincipal UserDetails user) {
        return patientService.findByAccountUsername(user.getUsername());
    }

    @GetMapping(path = "{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    public Patient getPatientById(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
       Patient patient = patientService.findOrThrow(id);
       if (!user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
            && !user.getUsername().equals(patient.getAccountUsername())) {
           boolean assigned = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"))
               && appointmentRepository.existsByPatientIdAndDoctorAccountUsername(id, user.getUsername());
           if (!assigned) throw new org.springframework.security.access.AccessDeniedException("Not permitted");
       }
       return patient;

    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("{id}")
    public Patient updateMethod(@PathVariable Long id,@Valid @RequestBody PatientReqDTO req){
        return patientService.updatePatient(id, req);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
 public void deleteMethod(@PathVariable Long id){
    patientService.deletePatient(id);
 }
}
