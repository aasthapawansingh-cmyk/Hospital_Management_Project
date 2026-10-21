package com.example.hosptial;

import java.util.List;
import java.util.Optional;
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
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("api/v1/doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final AppointmentRepository appointmentRepository;
    public DoctorController(DoctorService doctorService, AppointmentRepository appointmentRepository){
        this.doctorService = doctorService;
        this.appointmentRepository = appointmentRepository;
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
    public List<Doctor> getAvailableDoctors(@RequestParam LocalDate date, @RequestParam LocalTime time) {
        return doctorService.getAllDoctors().stream()
                .filter(d -> !appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNot(
                        d.getId(), date, time, AppointmentStatus.CANCELLED))
                .toList();
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
