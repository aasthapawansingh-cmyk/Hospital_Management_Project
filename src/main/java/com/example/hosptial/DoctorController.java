package com.example.hosptial;

import java.util.List;
import java.util.Optional;

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
    public DoctorController(DoctorService doctorService){
        this.doctorService = doctorService;
    }
    @PostMapping
    public void addDoctor( @RequestBody Doctor doctor){
         doctorService.addDoctor(doctor);
    }
    @GetMapping
    public List<Doctor> getAllDoctor(){
        return doctorService.getAllDoctors();
    }
    @GetMapping(path="{id}")
    public Optional<Doctor> getDoctorById(@PathVariable Long id){
        return doctorService.getDoctorById(id);
    }
     @DeleteMapping("{id}")
 public void deleteDoctor(@PathVariable Long id){
    doctorService.deleteDoctor(id);
 }
}
