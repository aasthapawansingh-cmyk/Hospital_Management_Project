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

@RestController
@RequestMapping("api/v1/patient")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void addPatient( @Valid @RequestBody PatientReqDTO req) {
        Patient patient =PatientMapper.toEntity(req);
        patientService.addPatient(patient);
    }
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping
    public Page<Patient> getAllPatient(Pageable pageable) {
        return patientService.getAllPatient(pageable);
    }

    @GetMapping(path = "{id}")
    public Patient getPatientById(@PathVariable Long id) {
       return patientService.findOrThrow(id);

    }
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
