package com.example.hosptial;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    public DoctorService(DoctorRepository doctorRepository){
        this.doctorRepository=doctorRepository;
    }
    public void addDoctor(Doctor doctor){
      doctorRepository.save(doctor);
    }
    public List<Doctor> getAllDoctors(){
       return doctorRepository.findAll();
    }
    public Optional<Doctor> getDoctorById(Long id){
        return doctorRepository.findById(id);
    }
    public void deleteDoctor(Long id){
        doctorRepository.deleteById(id);
    }
}
