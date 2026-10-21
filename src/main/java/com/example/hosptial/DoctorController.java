package com.example.hosptial;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    public DoctorController(DoctorService doctorService, AppointmentService appointmentService){
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public void addDoctor( @RequestBody Doctor doctor){
         doctorService.addDoctor(doctor);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Doctor> getAllDoctor(){
        return doctorService.getAllDoctors();
    }
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT')")
    @GetMapping("/available")
    public List<Doctor> getAvailableDoctors(@org.springframework.web.bind.annotation.RequestParam LocalDate date,
                                             @org.springframework.web.bind.annotation.RequestParam LocalTime time) {
        return appointmentService.getAvailableDoctors(date, time);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(path="{id}")
    public Optional<Doctor> getDoctorById(@PathVariable Long id){
        return doctorService.getDoctorById(id);
    }
     @PreAuthorize("hasRole('ADMIN')")
     @DeleteMapping("{id}")
 public void deleteDoctor(@PathVariable Long id){
    doctorService.deleteDoctor(id);
 }
}
